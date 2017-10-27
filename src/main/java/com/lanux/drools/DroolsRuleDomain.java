package com.lanux.drools;

public class DroolsRuleDomain {
    /** 数据库记录ID */
    private long id;
    /** 规则名称 */
    private String ruleName;
    /** 规则正文  */
    private String ruleContext;
    /** 规则版本 */
    private int version;
    /** 规则脚本状态 */
    private int status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleContext() {
        return ruleContext;
    }

    public void setRuleContext(String ruleContext) {
        this.ruleContext = ruleContext;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
