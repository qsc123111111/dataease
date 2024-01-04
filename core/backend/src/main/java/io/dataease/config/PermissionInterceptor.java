package io.dataease.config;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
@Component
public class PermissionInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSqlNew = (BoundSql) args[5];
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        String processSql = boundSql.getSql();
        String sql2Reset = processSql;
        if (sql2Reset.contains("`")) {
            System.out.println("断点");
            // 获取BoundSql对象的sql字段
            Field sqlField = BoundSql.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            String originalSql = (String) sqlField.get(boundSql);
            String modifiedSql = originalSql.replaceAll("`", "\"");
            // 使用反射修改参数中的BoundSql对象的sql字段
            sqlField.set(boundSql, modifiedSql);
            sqlField.set(boundSqlNew, modifiedSql);
        }
        // 修改SQL语句
        return invocation.proceed();
    }

    private void reflectAttributeValue(Object object, String attributeName, String attributeValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(attributeName);
        field.setAccessible(true);
        field.set(object, attributeValue);
    }

    private void modifyParameter(Object parameter,BoundSql boundSql) throws IllegalAccessException {
        Class<?> clazz = parameter.getClass();

        // 遍历参数的字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);

            // 修改字段的值
            Object value = field.get(parameter);
            if (value instanceof String) {
                String modifiedValue = ((String) value).toUpperCase();
                field.set(parameter, modifiedValue);
            }
        }
    }

}
