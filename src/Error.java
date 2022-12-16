package compiladorcool;

public abstract class Error {
    protected int row;
    
    public Error(int row)
    {
        this.row=row;
    }
    
    public abstract String getMessage();
    
}
