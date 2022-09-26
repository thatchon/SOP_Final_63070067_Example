package com.example.sop_final_63070067;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Route(value = "")
public class ProductView extends VerticalLayout {

    private ComboBox p_list;
    private TextField p_name;
    private NumberField p_cost, p_profit, p_price;
    private Button p_add, p_update, p_delete, p_clear;
    private HorizontalLayout panel;
    private ArrayList<String> productListName;
    private Notification noti;

    private Product fromProductPOJO;

    public ProductView() {

        productListName = new ArrayList<>();
        panel = new HorizontalLayout();
        p_list = new ComboBox("Product List:");
        p_name = new TextField("Product Name:");
        p_cost = new NumberField("Product Cost:");
        p_profit = new NumberField("Product Profit:");
        p_price = new NumberField("Product Price");
        p_add = new Button("Add Product");
        p_update = new Button("Update Product");
        p_delete = new Button("Delete Product");
        p_clear = new Button("Clear Product");
        noti = new Notification();

        panel.add(p_add, p_update, p_delete, p_clear);

        p_list.setWidth("600px");
        p_name.setWidth("600px");
        p_cost.setWidth("600px");
        p_profit.setWidth("600px");
        p_price.setWidth("600px");
        p_price.setEnabled(false);
        this.add(p_list, p_name, p_cost, p_profit, p_price, panel);
        clearProduct();
        productList();

        p_add.addClickListener(event -> {
            callPrice();
            try{
                String name = p_name.getValue();
                double cost = p_cost.getValue();
                double profit = p_profit.getValue();
                double price = p_price.getValue();
                Product newProduct = new Product(null, name, cost, profit, price);
                boolean status = WebClient.create()
                        .post()
                        .uri("http://127.0.0.1:8080/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(newProduct), Product.class)
                        .retrieve().bodyToMono(Boolean.class).block();
                if(status){
                    noti.show("Add complete");
                    productList();
                }
            }catch (Exception e){
                System.out.println(e);
            }
        });

        p_update.addClickListener(event -> {
            callPrice();
            String name = p_name.getValue();
            System.out.println(name);
            double cost = p_cost.getValue();
            double profit = p_profit.getValue();
            double price = p_price.getValue();
            Product updateProduct = new Product(fromProductPOJO.get_id(), name, cost, profit, price);
            boolean status = WebClient.create()
                    .post()
                    .uri("http://127.0.0.1:8080/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(updateProduct), Product.class)
                    .retrieve().bodyToMono(Boolean.class).block();
            productList();
            p_list.setValue(name);

            if(status){
                noti.show("Update complete");
            }
        });

        p_delete.addClickListener(event -> {

        });

        p_cost.addKeyPressListener(Key.ENTER, e ->{
            callPrice();
        });

        p_profit.addKeyPressListener(Key.ENTER, e ->{
            callPrice();
        });

        p_list.addValueChangeListener(event ->{
            if(!p_list.getValue().equals("")){
                fromProductPOJO = WebClient.create().get()
                        .uri("http://127.0.0.1:8080/getProduct/"+ p_list.getValue())
                        .retrieve().bodyToMono(Product.class).block();
                p_name.setValue(fromProductPOJO.getProductName());
                p_cost.setValue(Double.valueOf(fromProductPOJO.getProductCost() + ""));
                p_profit.setValue(Double.valueOf(fromProductPOJO.getProductProfit()+""));
                p_price.setValue(fromProductPOJO.getProductPrice());
            }
        });

        p_clear.addClickListener(event -> {
            clearProduct();
            productList();
            noti.show("Clear");
            noti.setDuration(500);
            noti.setPosition(Notification.Position.BOTTOM_START);
        });

    }

    private void clearProduct() {
        p_name.setValue("");
        p_cost.setValue(0.0);
        p_profit.setValue(0.0);
        p_price.setValue(0.0);
    }

    public void productList(){
        ArrayList<Product> productAll = WebClient.create()
                .get()
                .uri("http://127.0.0.1:8080/getAllProduct")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ArrayList<Product>>() {})
                .block();
        productListName.clear();
        for(int i = 0; i < productAll.size(); i++){
            productListName.add(productAll.get(i).getProductName());
        }
        p_list.setItems(productListName);
    }

    public void callPrice(){
        double numCost = p_cost.getValue();
        double numProfit = p_profit.getValue();
        double number = WebClient.create()
                .get()
                .uri("http://127.0.0.1:8080/getPrice/"+ numCost +"/"+ numProfit)
                .retrieve()
                .bodyToMono(Double.class)
                .block();
        p_price.setValue(number);
    }

}

