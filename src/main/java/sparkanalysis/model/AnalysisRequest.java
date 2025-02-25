package sparkanalysis.model;

public class AnalysisRequest {
    private String filePath;
    private Double threshold;
    private String operation;
    private String[] columns;
    private String condition;
    private Integer limit;
    private String outputTable;
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public Double getThreshold() {
        return threshold;
    }
    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }
    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
    public String[] getColumns() {
        return columns;
    }
    public void setColumns(String[] columns) {
        this.columns = columns;
    }
    public String getCondition() {
        return condition;
    }
    public void setCondition(String condition) {
        this.condition = condition;
    }
    public Integer getLimit() {
        return limit;
    }
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getOutputTable() {
        return outputTable;
    }
    public void setOutputTable(String outputTable) {
        this.outputTable = outputTable;
    }
}
