package dev.sora.itemcreator.gui;

public class RecipeCreationResult {
    private final boolean success;
    private final String error;
    
    private RecipeCreationResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }
    
    public static RecipeCreationResult success() {
        return new RecipeCreationResult(true, null);
    }
    
    public static RecipeCreationResult failure(String error) {
        return new RecipeCreationResult(false, error);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getError() {
        return error;
    }
}
