package fx;

public class MovieModel {

    private String list;

    /**
     * Helps organize list of all title in UI
     * @param list
     */
    public MovieModel(String list) {
        this.list = list;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }
}
