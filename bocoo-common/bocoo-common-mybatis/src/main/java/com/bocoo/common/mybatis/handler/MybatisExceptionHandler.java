package com.bocoo.common.mybatis.handler;

import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import com.bocoo.common.core.utils.StringUtils;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

import java.sql.SQLSyntaxErrorException;

/**
 * Mybatis异常处理器
 *
 * @author cmx
 */
@Slf4j
@RestControllerAdvice
public class MybatisExceptionHandler {

    /**
     * 主键或UNIQUE索引，数据重复异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public R<Void> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',数据库中已存在记录'{}'", requestURI, e.getMessage());
        return R.fail(MessageUtils.message("db.record.exists"));
    }

    /**
     * Mybatis系统异常 通用处理
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public R<Void> handleCannotFindDataSourceException(MyBatisSystemException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String message = e.getMessage();
        if (StringUtils.contains("CannotFindDataSourceException", message)) {
            log.error("请求地址'{}', 未找到数据源", requestURI);
            return R.fail(MessageUtils.message("db.datasource.notFound"));
        }
        log.error("请求地址'{}', Mybatis系统异常", requestURI, e);
        return R.fail(message);
    }


    /**
     * SQL语法错误异常处理
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    public R<Void> handleBadSqlGrammarException(BadSqlGrammarException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}', SQL语法错误: {}", requestURI, e.getMessage());

        // 获取底层SQL异常
        SQLSyntaxErrorException sqlException = (SQLSyntaxErrorException) e.getCause();
        String errorMessage = sqlException.getMessage();

        // 针对特定错误进行处理
        if (errorMessage.contains("Unknown column")) {
            return R.fail(MessageUtils.message("db.schema.mismatch"));
        }

        return R.fail(MessageUtils.message("db.operation.error"));
    }

}
