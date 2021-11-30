package mint.model.walk.probabilistic;

import java.util.List;
import java.util.Objects;

class ListAndElement<T> {
    private List list;

    public List getList() {
        return list;
    }

    public T getElement() {
        return element;
    }

    private T element;

    public ListAndElement(List list, T element) {
        this.list = list;
        this.element = element;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListAndElement that = (ListAndElement) o;
        return listString(list).equals(listString(that.list)) && element.equals(that.element);
    }

    @Override
    public int hashCode() {


        return Objects.hash(listString(list), element);
    }

    private String listString(List toConvert){
        String s = "";
        for (Object o : toConvert) {
            s += o.toString();
        }
        return s;
    }
}
