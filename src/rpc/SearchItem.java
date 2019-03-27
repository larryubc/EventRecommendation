package rpc;


import entity.Item;
import external.TicketMasterAPI;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


@WebServlet("/search")
public class SearchItem extends HttpServlet {


    public SearchItem() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONArray array = new JSONArray();
        try {
            System.out.println(request);
            double lat = Double.parseDouble(request.getParameter("lat"));
            double lon = Double.parseDouble(request.getParameter("lon"));
            String keyword = request.getParameter("term");

            TicketMasterAPI tmAPI = new TicketMasterAPI();

            List<Item> items = tmAPI.search(lat,lon,keyword);

            for (Item item : items) {
                JSONObject obj = item.toJSONObject();
                array.put(obj);
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(request);
        }

        RpcHelper.writeJsonArray(response,array);


    }
}
