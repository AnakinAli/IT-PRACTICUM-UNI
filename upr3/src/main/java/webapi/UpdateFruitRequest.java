package webapi;

public class UpdateFruitRequest {
    private String oldFruit;
    private String newFruit;
    public UpdateFruitRequest() { }
    public String getOldFruit() { return oldFruit; }
    public void setOldFruit(String oldFruit) { this.oldFruit = oldFruit; }
    public String getNewFruit() { return newFruit; }
    public void setNewFruit(String newFruit) { this.newFruit = newFruit; }
}
