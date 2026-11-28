package com.pgmmers.radar.service.impl.engine.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.service.engine.strategy.StrategyOutcome;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * L1 纯单测：决策树执行器（DecisionTreeExecutor）。
 * 覆盖：路径走查（true/false 分支）、叶决策直出、命中路径明细、环防护、脏数据防护。
 */
class DecisionTreeExecutorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final DecisionTreeExecutor executor = new DecisionTreeExecutor();
    private Map<String, Map<String, ?>> data;
    private Map<String, Object> lists;

    @BeforeEach
    void setUp() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("amount", 100.0);
        Map<String, Object> abstractions = new HashMap<>();
        abstractions.put("tran_ip_1_day_qty", 35);
        data = new HashMap<>();
        data.put("fields", fields);
        data.put("features", abstractions);

        Map<String, String> whiteUser = new HashMap<>();
        whiteUser.put("u_1001", "");
        lists = new HashMap<>();
        lists.put("white_user", whiteUser);
    }

    private StrategyVO strategy(String definitionJson) throws Exception {
        StrategyVO vo = new StrategyVO();
        vo.setStrategyName("tree_1");
        vo.setDefinition(MAPPER.readTree(definitionJson));
        return vo;
    }

    private static String cond(String id, String label, String conditionJson, String trueChild, String falseChild) {
        return "{\"id\":\"" + id + "\",\"type\":\"CONDITION\",\"label\":\"" + label + "\","
                + "\"condition\":" + conditionJson + ","
                + "\"trueChild\":\"" + trueChild + "\",\"falseChild\":\"" + falseChild + "\"}";
    }

    private static String leaf(String id, String label, String decision, int score, String reason) {
        return "{\"id\":\"" + id + "\",\"type\":\"LEAF\",\"label\":\"" + label + "\","
                + "\"decision\":\"" + decision + "\",\"score\":" + score + ",\"reason\":\"" + reason + "\"}";
    }

    /** 示例树：白名单? →(Y) pass / (N) IP频次>=30? →(Y) reject / (N) review */
    private String sampleTree() {
        String whiteCheck = "{\"class\":\"SMPL\",\"operator\":\"InList\",\"expressions\":["
                + "{\"class\":\"ENTATTR\",\"column\":\"fields.userId\"},"
                + "{\"class\":\"CONST\",\"type\":\"LIST\",\"value\":\"white_user\"}]}";
        String ipFrequent = "{\"class\":\"SMPL\",\"operator\":\"Greater_Equal\",\"expressions\":["
                + "{\"class\":\"ENTATTR\",\"column\":\"features.tran_ip_1_day_qty\"},"
                + "{\"class\":\"CONST\",\"type\":\"DOUBLE\",\"value\":\"30\"}]}";
        return "{\"rootId\":\"n1\",\"nodes\":["
                + cond("n1", "白名单?", whiteCheck, "n2", "n3") + ","
                + leaf("n2", "白名单通过", "pass", 0, "命中白名单") + ","
                + cond("n3", "IP高频?", ipFrequent, "n4", "n5") + ","
                + leaf("n4", "IP高频拒绝", "reject", 100, "IP频次异常") + ","
                + leaf("n5", "人工审核", "review", 50, "常规人工审核")
                + "]}";
    }

    @Test
    @DisplayName("路径走查：白名单命中 → pass 叶")
    void whiteListPath() throws Exception {
        Map<String, Object> fields = new HashMap<>(data.get("fields"));
        fields.put("userId", "u_1001");
        data.put("fields", fields);
        StrategyOutcome outcome = executor.execute(strategy(sampleTree()), data, lists);
        assertEquals("pass", outcome.getRisk());
        assertEquals(0, outcome.getScore().intValue());
        // 路径明细：n1(Y) + 叶 n2
        assertEquals(2, outcome.getHits().size());
        assertEquals("白名单?(Y)", outcome.getHits().get(0).getDesc());
        assertEquals("命中白名单", outcome.getHits().get(1).getDesc());
    }

    @Test
    @DisplayName("路径走查：非白名单 + IP高频 → reject 叶")
    void rejectPath() throws Exception {
        Map<String, Object> fields = new HashMap<>(data.get("fields"));
        fields.put("userId", "u_9999");
        data.put("fields", fields);
        StrategyOutcome outcome = executor.execute(strategy(sampleTree()), data, lists);
        assertEquals("reject", outcome.getRisk());
        assertEquals(100, outcome.getScore().intValue());
        // 路径明细：n1(N) + n3(Y) + 叶 n4
        assertEquals(3, outcome.getHits().size());
        assertEquals("白名单?(N)", outcome.getHits().get(0).getDesc());
        assertEquals("IP高频?(Y)", outcome.getHits().get(1).getDesc());
        assertEquals("IP频次异常", outcome.getHits().get(2).getDesc());
        assertTrue(outcome.getHits2().containsKey("node_n4"));
    }

    @Test
    @DisplayName("路径走查：非白名单 + IP不高频 → review 叶")
    void reviewPath() throws Exception {
        Map<String, Object> fields = new HashMap<>(data.get("fields"));
        fields.put("userId", "u_9999");
        Map<String, Object> abstractions = new HashMap<>(data.get("features"));
        abstractions.put("tran_ip_1_day_qty", 5);
        data.put("fields", fields);
        data.put("features", abstractions);
        StrategyOutcome outcome = executor.execute(strategy(sampleTree()), data, lists);
        assertEquals("review", outcome.getRisk());
        assertEquals(50, outcome.getScore().intValue());
    }

    @Test
    @DisplayName("环防护：环路抛出异常（编排层隔离）")
    void cycleGuard() throws Exception {
        String loop = "{\"rootId\":\"n1\",\"nodes\":["
                + cond("n1", "永真", "{\"class\":\"SMPL\",\"operator\":\"IsNotNull\",\"expressions\":[{\"class\":\"ENTATTR\",\"column\":\"fields.amount\"}]}", "n1", "n1")
                + "]}";
        assertThrows(IllegalStateException.class,
                () -> executor.execute(strategy(loop), data, lists));
    }

    @Test
    @DisplayName("脏数据：分支缺失/叶决策非法/重复 id 抛异常")
    void dirtyDefinitions() throws Exception {
        // 分支指向不存在节点：执行期按访问路径惰性校验——被走到的悬空分支才报错
        // （全树完整性校验属于编辑器保存前职责）
        String dangling = "{\"rootId\":\"n1\",\"nodes\":["
                + cond("n1", "条件", "{\"class\":\"SMPL\",\"operator\":\"IsNotNull\",\"expressions\":[{\"class\":\"ENTATTR\",\"column\":\"fields.amount\"}]}", "ghost", "n2") + ","
                + leaf("n2", "通过", "pass", 0, "")
                + "]}";
        assertThrows(IllegalArgumentException.class, () -> executor.execute(strategy(dangling), data, lists));
        // 叶 decision 非法
        String badDecision = "{\"rootId\":\"n1\",\"nodes\":["
                + leaf("n1", "坏叶", "maybe", 0, "") + "]}";
        assertThrows(IllegalArgumentException.class, () -> executor.execute(strategy(badDecision), data, lists));
        // 节点 id 重复
        String duplicate = "{\"rootId\":\"n1\",\"nodes\":["
                + cond("n1", "条件", "{\"class\":\"SMPL\",\"operator\":\"IsNotNull\",\"expressions\":[{\"class\":\"ENTATTR\",\"column\":\"fields.amount\"}]}", "n2", "n3") + ","
                + leaf("n2", "通过", "pass", 0, "") + ","
                + leaf("n2", "重复id", "pass", 0, "") + "]}";
        assertThrows(IllegalArgumentException.class, () -> executor.execute(strategy(duplicate), data, lists));
    }

    @Test
    @DisplayName("definition 缺失抛出异常")
    void missingDefinition() {
        StrategyVO vo = new StrategyVO();
        vo.setStrategyName("broken_tree");
        assertThrows(IllegalArgumentException.class, () -> executor.execute(vo, data, lists));
    }
}
