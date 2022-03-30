package edu.bsu.cs411.myweatheralert;

public class Alert {

    private final String title;
    private final String link;
    private final String summary;

    Alert(String title, String link, String summary) {
        this.title = title;
        this.link = link;
        this.summary = summary;
    }

    public String getLink()
    { return link; }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        String divider = "--------------------";
        String asteriskDiv = "********************";
        return String.format("%nNEW ALERT" +
                        "%n%s%n%nTITLE %n--> " +
                        " %s%n%s%nLINK %n--> " +
                        " %s%n%s%nSUMMARY %n--> " +
                        " %s%n%n%s%nEND ALERT%n",
                asteriskDiv, title, divider, link, divider, summary, asteriskDiv);
    }
}
