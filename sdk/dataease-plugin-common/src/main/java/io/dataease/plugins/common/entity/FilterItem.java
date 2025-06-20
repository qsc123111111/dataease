package io.dataease.plugins.common.entity;

import lombok.Builder;
import lombok.Data;

@Data

public class FilterItem {
    private String term;
    private String value;

    public void setTerm(String term) {
        switch (term) {
            case "=":
                this.term =  "eq";
                break;
            case "!=":
                this.term =  "not_eq";
                break;
            case "<":
                this.term =  "lt";
                break;
            case "<=":
                this.term =  "le";
                break;
            case ">":
                this.term =  "gt";
                break;
            case ">=":
                this.term =  "ge";
                break;
            default:
                this.term =  "";
        }
    }
}
