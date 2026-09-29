package Users;
//abstract class for all users
public abstract class User{
    String username;
    String password;

    User(String username, String password)
    {
        this.username=username;
        this.password=password;
    }

    abstract void display();

    public String getUsername()
    {
        return username;
    }
    String getPassword()
    {
        return password;
    }

    public void setPassword(String newpassword) {
        this.password=newpassword;
    }

}