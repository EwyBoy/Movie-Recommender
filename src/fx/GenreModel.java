package fx;

public class GenreModel {

    private String list;

    /**
     * This model is used to help generate the genre list entries
     * @param list
     */
    public GenreModel(String list) {
        this.list = list;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }
}
