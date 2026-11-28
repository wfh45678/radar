package com.pgmmers.radar.service.impl.engine.feature;

import com.pgmmers.radar.service.engine.feature.DerivedFunction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * 内置衍生函数集（v4.1 首批 13 个）：算术/字符串/映射/空值处理。
 * 数值函数对非数值参数返回 null；扩展新函数实现 {@link DerivedFunction} 即可。
 */
public final class StandardDerivedFunctions {

    private StandardDerivedFunctions() {
    }

    private static BigDecimal number(Object v) {
        if (v == null) {
            return null;
        }
        try {
            return new BigDecimal(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String str(Object v) {
        return v == null ? null : v.toString();
    }

    /** 算术四则（+−×÷）：数值运算；DIV 除数为 0 返回 null。 */
    public abstract static class Arithmetic implements DerivedFunction {
        private final String name;
        private final char op;

        protected Arithmetic(String name, char op) {
            this.name = name;
            this.op = op;
        }

        @Override
        public final String name() {
            return name;
        }

        @Override
        public final int minArgs() {
            return 2;
        }

        @Override
        public final int maxArgs() {
            return 2;
        }

        @Override
        public final Object apply(List<Object> args) {
            BigDecimal left = number(args.get(0));
            BigDecimal right = number(args.get(1));
            if (left == null || right == null) {
                return null;
            }
            switch (op) {
            case '+':
                return left.add(right);
            case '-':
                return left.subtract(right);
            case '*':
                return left.multiply(right);
            default:
                if (BigDecimal.ZERO.compareTo(right) == 0) {
                    return null;
                }
                return left.divide(right, 4, RoundingMode.HALF_UP).stripTrailingZeros();
            }
        }
    }

    @Component
    public static class Add extends Arithmetic {
        public Add() {
            super("ADD", '+');
        }
    }

    @Component
    public static class Sub extends Arithmetic {
        public Sub() {
            super("SUB", '-');
        }
    }

    @Component
    public static class Mul extends Arithmetic {
        public Mul() {
            super("MUL", '*');
        }
    }

    @Component
    public static class Div extends Arithmetic {
        public Div() {
            super("DIV", '/');
        }
    }

    /** 绝对值。 */
    @Component
    public static class Abs implements DerivedFunction {
        @Override
        public String name() {
            return "ABS";
        }

        @Override
        public int minArgs() {
            return 1;
        }

        @Override
        public int maxArgs() {
            return 1;
        }

        @Override
        public Object apply(List<Object> args) {
            BigDecimal v = number(args.get(0));
            return v == null ? null : v.abs();
        }
    }

    /** 四舍五入（scale 为第二个参数，缺省 2）。 */
    @Component
    public static class Round implements DerivedFunction {
        @Override
        public String name() {
            return "ROUND";
        }

        @Override
        public int minArgs() {
            return 1;
        }

        @Override
        public int maxArgs() {
            return 2;
        }

        @Override
        public Object apply(List<Object> args) {
            BigDecimal v = number(args.get(0));
            if (v == null) {
                return null;
            }
            int scale = args.size() > 1 && args.get(1) != null ? number(args.get(1)).intValue() : 2;
            return v.setScale(Math.max(0, scale), RoundingMode.HALF_UP);
        }
    }

    /** 字符串拼接（任意数量参数，null 当空串）。 */
    @Component
    public static class Concat implements DerivedFunction {
        @Override
        public String name() {
            return "CONCAT";
        }

        @Override
        public int minArgs() {
            return 1;
        }

        @Override
        public int maxArgs() {
            return -1;
        }

        @Override
        public Object apply(List<Object> args) {
            StringBuilder sb = new StringBuilder();
            for (Object arg : args) {
                sb.append(arg == null ? "" : arg.toString());
            }
            return sb.toString();
        }
    }

    /** 子串（start/end 下标，end 缺省到末尾）。 */
    @Component
    public static class Substring implements DerivedFunction {
        @Override
        public String name() {
            return "SUBSTRING";
        }

        @Override
        public int minArgs() {
            return 2;
        }

        @Override
        public int maxArgs() {
            return 3;
        }

        @Override
        public Object apply(List<Object> args) {
            String s = str(args.get(0));
            BigDecimal start = number(args.get(1));
            if (s == null || start == null) {
                return null;
            }
            int begin = Math.max(0, start.intValue());
            if (begin > s.length()) {
                return "";
            }
            if (args.size() > 2 && args.get(2) != null) {
                BigDecimal end = number(args.get(2));
                if (end == null) {
                    return null;
                }
                int endIdx = Math.min(s.length(), Math.max(begin, end.intValue()));
                return s.substring(begin, endIdx);
            }
            return s.substring(begin);
        }
    }

    @Component
    public static class Upper implements DerivedFunction {
        @Override
        public String name() {
            return "UPPER";
        }

        @Override
        public int minArgs() {
            return 1;
        }

        @Override
        public int maxArgs() {
            return 1;
        }

        @Override
        public Object apply(List<Object> args) {
            String s = str(args.get(0));
            return s == null ? null : s.toUpperCase();
        }
    }

    @Component
    public static class Lower implements DerivedFunction {
        @Override
        public String name() {
            return "LOWER";
        }

        @Override
        public int minArgs() {
            return 1;
        }

        @Override
        public int maxArgs() {
            return 1;
        }

        @Override
        public Object apply(List<Object> args) {
            String s = str(args.get(0));
            return s == null ? null : s.toLowerCase();
        }
    }

    /** 正则提取：args = [文本, 正则]，取首个捕获组（无捕获组取整段匹配），不匹配返回 null。 */
    @Component
    public static class RegexExtract implements DerivedFunction {
        @Override
        public String name() {
            return "REGEX_EXTRACT";
        }

        @Override
        public int minArgs() {
            return 2;
        }

        @Override
        public int maxArgs() {
            return 2;
        }

        @Override
        public Object apply(List<Object> args) {
            String s = str(args.get(0));
            String regex = str(args.get(1));
            if (s == null || regex == null) {
                return null;
            }
            java.util.regex.Matcher matcher = Pattern.compile(regex).matcher(s);
            if (!matcher.find()) {
                return null;
            }
            return matcher.groupCount() > 0 ? matcher.group(1) : matcher.group();
        }
    }

    /** 映射表：args = [值, 匹配1, 结果1, 匹配2, 结果2, ...，缺省值（可选，末位）]。字符串等值比较。 */
    @Component
    public static class Mapping implements DerivedFunction {
        @Override
        public String name() {
            return "MAPPING";
        }

        @Override
        public int minArgs() {
            return 2;
        }

        @Override
        public int maxArgs() {
            return -1;
        }

        @Override
        public Object apply(List<Object> args) {
            String value = str(args.get(0));
            int pairs = (args.size() - 1) / 2;
            Map<String, Object> table = new LinkedHashMap<>();
            String defaultResult = null;
            for (int i = 0; i < pairs; i++) {
                table.put(str(args.get(1 + i * 2)), args.get(2 + i * 2));
            }
            if ((args.size() - 1) % 2 == 1) {
                defaultResult = str(args.get(args.size() - 1));
            }
            if (value != null && table.containsKey(value)) {
                return table.get(value);
            }
            return defaultResult;
        }
    }

    /** 空值兜底：返回第一个非 null 参数，全 null 返回 null。 */
    @Component
    public static class Coalesce implements DerivedFunction {
        @Override
        public String name() {
            return "COALESCE";
        }

        @Override
        public int minArgs() {
            return 1;
        }

        @Override
        public int maxArgs() {
            return -1;
        }

        @Override
        public Object apply(List<Object> args) {
            for (Object arg : args) {
                if (arg != null) {
                    return arg;
                }
            }
            return null;
        }
    }
}
