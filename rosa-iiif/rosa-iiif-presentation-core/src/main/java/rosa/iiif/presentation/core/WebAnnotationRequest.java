package rosa.iiif.presentation.core;

public enum WebAnnotationRequest {
    ANNOTATION("annotation"),
    ANNOTATION_PAGE("annotationpage"),
    ANNOTATION_COLLECTION("annotationcollection");
    
    private final String pathname;
    
    private WebAnnotationRequest(String pathname) {
        this.pathname = pathname;
    }
    
    public String getPathName() {
        return pathname;
    }
    
}
