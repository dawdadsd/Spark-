package sparkanalysis.model;

public class CleanedData {
    private String dataId;

    public String getCleanType() {
        return cleanType;
    }

    public void setCleanType(String cleanType) {
        this.cleanType = cleanType;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getCleanedContent() {
        return cleanedContent;
    }

    public void setCleanedContent(String cleanedContent) {
        this.cleanedContent = cleanedContent;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getCleanTimestamp() {
        return cleanTimestamp;
    }

    public void setCleanTimestamp(String cleanTimestamp) {
        this.cleanTimestamp = cleanTimestamp;
    }

    private String cleanedContent;
    private String cleanType;
    private String storageLocation;
    private String cleanTimestamp;
} 