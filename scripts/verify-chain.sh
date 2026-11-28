#!/usr/bin/env bash
# 部署后全链路验证（复刻 v2.0 冒烟十步）——在应用所在机器执行，依赖 curl/python3/docker。
# 验证码绕过：直写 Redis db1 的 log_captcha_<CODE>（见 CacheServiceImpl.LOGIN_CAPTCHA_PREFIX）。
set -euo pipefail
BASE=${BASE:-http://localhost:6580}
REDIS_CONTAINER=${REDIS_CONTAINER:-radar-redis}
REDIS_PASS=${REDIS_PASS:-radar123}
TS=$(date +%s)
LOGIN="it_$TS"; PASS="pass_$TS"; LABEL="IT链路_$TS"; REQID="it_req_$TS"
CAPTCHA="ITCAP$TS"

step() { echo "==> [$1] $2"; }
assert_ok() { python3 -c "import json,sys;d=json.loads(sys.argv[1]);assert d.get('success'), sys.argv[1]" "$1"; }

# 1. 验证码
step 1 "写入验证码到 Redis db1"
docker exec "$REDIS_CONTAINER" redis-cli -a "$REDIS_PASS" -n 1 SET "log_captcha_${CAPTCHA}" "$CAPTCHA" EX 300 > /dev/null

# 2. 注册
step 2 "注册 $LOGIN"
R=$(curl -s -X POST "$BASE/services/v1/user/regist" -d "loginName=$LOGIN" -d "passwd=$PASS" -d "verifyPasswd=$PASS" -d "captcha=$CAPTCHA")
assert_ok "$R"

# 3. 登录
step 3 "登录获取 JWT"
R=$(curl -s -X POST "$BASE/services/v1/user/login" -d "loginName=$LOGIN" -d "passwd=$PASS" -d "captcha=$CAPTCHA")
assert_ok "$R"
TOKEN=$(echo "$R" | python3 -c "import json,sys;print(json.loads(sys.stdin.read())['data']['x-auth-token'])")
AUTH="x-auth-token: $TOKEN"
echo "    token len=${#TOKEN}"

# 4. 模板列表含 103
step 4 "模板列表含种子模板 103"
R=$(curl -s -H "$AUTH" "$BASE/services/v1/model/list/template")
echo "$R" | python3 -c "
import json,sys
d=json.load(sys.stdin); assert d.get('success'), d
ids=[m['id'] for m in d['data']['modelList']]
assert 103 in ids, f'103 not in {ids}'
print('    OK')"

# 5. copy 模板
step 5 "copy 模板 103"
R=$(curl -s -X POST -H "$AUTH" -H 'Content-Type: application/json' \
    -d "{\"id\":103,\"modelName\":\"it_model_$TS\",\"label\":\"$LABEL\"}" \
    "$BASE/services/v1/model/copy")
assert_ok "$R"

# 6. 模型列表定位新模型
step 6 "模型列表定位新模型"
R=$(curl -s -H "$AUTH" "$BASE/services/v1/model/list")
read -r MODEL_ID MODEL_GUID <<< "$(echo "$R" | python3 -c "
import json,sys
d=json.load(sys.stdin)
rows=d.get('data',{}).get('modelList') or d.get('data',{}).get('list') or []
m=[x for x in rows if x.get('label')=='$LABEL']
assert m, f'not found: {d}'
print(m[0]['id'], m[0].get('guid') or m[0].get('GUID'))")"
echo "    modelId=$MODEL_ID guid=$MODEL_GUID"

# 7/8. build + enable
step 7 "build 模型"
assert_ok "$(curl -s -X POST -H "$AUTH" "$BASE/services/v1/model/build/$MODEL_ID")"
step 8 "enable 模型"
assert_ok "$(curl -s -X POST -H "$AUTH" "$BASE/services/v1/model/enable/$MODEL_ID")"

# 9. 事件上传（两条——频次类规则需第二笔才达阈值，同 v2.0 冒烟口径）
step 9 "上传两条事件，断言规则命中"
EVENT="{\"eventId\":\"$REQID\",\"mobile\":\"18516249909\",\"userId\":\"$LOGIN\",\"eventTime\":$(date +%s000),\"userIP\":\"180.175.166.148\",\"deviceId\":\"IT-DEVICE\",\"os\":\"ios\",\"amount\":500.0,\"channel\":\"alipay\"}"
R=$(curl -s -X POST "$BASE/services/v1/uploadInfo" -d "modelGuid=$MODEL_GUID" -d "reqId=$REQID" --data-urlencode "jsonInfo=$EVENT")
assert_ok "$R"
EVENT2="{\"eventId\":\"${REQID}_b\",\"mobile\":\"18516249909\",\"userId\":\"$LOGIN\",\"eventTime\":$(date +%s000),\"userIP\":\"180.175.166.148\",\"deviceId\":\"IT-DEVICE\",\"os\":\"ios\",\"amount\":500.0,\"channel\":\"alipay\"}"
R=$(curl -s -X POST "$BASE/services/v1/uploadInfo" -d "modelGuid=$MODEL_GUID" -d "reqId=${REQID}_b" --data-urlencode "jsonInfo=$EVENT2")
assert_ok "$R"
echo "$R" | python3 -c "
import json,sys
d=json.load(sys.stdin)
# v4.1 契约：顶层 strategies（v4.0 为 activations，随特征工程统一更名）
strs=d.get('strategies') or {}
assert strs, f'no strategies: {d}'
hits=d.get('hitsDetail') or {}
total_hits=sum(len(v) for v in hits.values())
print(f'    decisions: {json.dumps({k: v[\"risk\"]+\"/\"+str(v.get(\"score\",\"?\")) for k,v in strs.items()}, ensure_ascii=False)}, hits={total_hits}')"

# 10a. getScore
step "10a" "getScore（Redis 通道）"
R=$(curl -s -H "$AUTH" "$BASE/services/v1/getScore?modelGuid=$MODEL_GUID&reqId=$REQID")
assert_ok "$R"

# 10b. hitsSort 报表
step "10b" "规则命中报表"
BEGIN=$(date -d '-1 day' '+%Y-%m-%d %H:%M:%S'); END=$(date -d '+1 day' '+%Y-%m-%d %H:%M:%S')
R=$(curl -s -X POST -H "$AUTH" -H 'Content-Type: application/json' \
    -d "{\"modelId\":$MODEL_ID,\"beginTime\":\"$BEGIN\",\"endTime\":\"$END\"}" \
    "$BASE/services/v1/rule/hitsSort/$MODEL_ID")
assert_ok "$R"

echo ""
echo "===== 全链路验证通过 ====="
echo "事件 QPS 指标: curl -s $BASE/actuator/prometheus | grep radar_"
