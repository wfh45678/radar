package com.pgmmers.radar.intercpt;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 添加和获取本地线程缓存信息
 * @author xushuai
 */
@Component
public class ContextHolder {
    private ThreadLocal<Context> contextThreadLocal = new ThreadLocal();

    public ContextHolder() {
    }

    public boolean available() {
        return this.contextThreadLocal.get() != null;
    }

    public void putContext(Context context) {
        this.contextThreadLocal.set(context);
    }

    public Context getContext() {
        Context ctx = this.contextThreadLocal.get();
        if (ctx == null) {
            ctx = new Context();
            this.contextThreadLocal.set(ctx);
        }

        return ctx;
    }

    public void clearContext() {
        this.contextThreadLocal.remove();
    }

    public <T> T getAttributeByType(String name, Class<T> t) {
        try {
            return (T) Optional.ofNullable(this.getContext().getAttributes()).map((m) -> {
                return m.get(name);
            }).orElse((Object)null);
        } catch (Exception var4) {
            return null;
        }
    }
}
