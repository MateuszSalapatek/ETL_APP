package sample;


import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.ArrayList;

public class Controller {

    private static String html = "https://www.filmweb.pl/film/Kler-2018-810402/discussion?plusMinus=false&page=";

    @FXML
    private TextArea tAContentText;

    @FXML
    private void initialize() throws IOException {
        ArrayList<Comment> list = getComments();
        System.out.println(list.size());
        for (int i = 0; i<list.size(); i++){
            tAContentText.setText(tAContentText.getText()+ "Autor: "+ list.get(i).getUser() + "---- Komentarz: " + list.get(i).getCommentContent()+
                    "---- Data utworzenia: "+ list.get(i).getCreationDate()+"\n");

        }




//        TESTY///////////////////////
        //        Elements test = doc.getElementsByClass("topics-list").select(".filmCategory");
//        System.out.println(test.attr("id","topic").get(28).select(".text").html()); //content
//        System.out.println(test.attr("id","topic").get(28).select(".userNameLink").html()); //user
//        System.out.println(test.attr("id","topic").get(28).select(".topicInfo .cap").html()); //data dodania
//        System.out.println(test.attr("id","topic").get(28).select(".topicInfo li:nth-child(3)").html()); //ocena



        // TODO jak pobrać wartość id?
        // TODO niektórzy nie oceniają
        // TODO uzytkownik może być usunięty
        // TODO na dacie 0200 i 0100 to chyba strefy czasowe, więc będzie trzeba dodać przy transformie

    }
    public ArrayList<Comment> getComments() {

        ArrayList<Comment> commentsList = new ArrayList<Comment>();
        try {
            int pageIterator = 1;
            Document doc = Jsoup.connect(html + pageIterator).get();
            while (doc.getElementsByClass("topics-list").select(".filmCategory").size() > 0) {

                for (int i = 0; i < doc.getElementsByClass("topics-list").select(".filmCategory").size(); i++) {
                    Elements fbBody = doc.getElementsByClass("topics-list").select(".filmCategory");

                    Comment commentObject = new Comment();

                    commentObject.setCommentContent(fbBody.attr("id", "topic").get(i).select(".text").html()); //  comment
                    if ( commentObject.getCommentContent().equals("")){
                        commentObject.setCommentContent(fbBody.attr("id","topic").get(i).select(".italic").html()); //content dla niezgosności z regulaminem);
                    }
                    commentObject.setUser(fbBody.attr("id", "topic").get(i).select(".userNameLink").html()); // user
                    commentObject.setCreationDate(fbBody.attr("id", "topic").get(i).select(".topicInfo .cap").html()); // creation date

                    commentsList.add(commentObject);
                }
                pageIterator++;
                doc = Jsoup.connect(html + pageIterator).get();
            }
        } catch (ExportException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            
        }
        return commentsList;
    }
}
