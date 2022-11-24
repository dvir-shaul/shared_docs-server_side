package docSharing.utils.debounce;

public interface Callback<T> {
    public void call(T t);
}