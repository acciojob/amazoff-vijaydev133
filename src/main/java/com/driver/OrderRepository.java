package com.driver;


import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class OrderRepository {

    //for storing orders list --> <OrderId, order>
    HashMap<String, Order> ordersDb = new HashMap<>();

    //for storing partners list --> <PartnerId, partner>
    HashMap<String, DeliveryPartner> partnersDb = new HashMap<>();

    //for storing which order assigned to which partner --> <OrderId, PartnerId>
    HashMap<String, String> orderToPartnerDb = new HashMap<>();

    //for storing the orders list assigned to a specific partner --> <PartnerId, List<OrdersId>>
    HashMap<String, List<String>> partnerToOrderDb = new HashMap<>();

    public void addOrder(Order order) {
        //Add this order in the Orders Map
        ordersDb.put(order.getId(), order);
    }

    public void addPartner(String partnerId) {
        //Add this partner in the Partners Map
        partnersDb.put(partnerId, new DeliveryPartner(partnerId));
    }

    public void addOrderPartnerPair(String orderId, String partnerId) {
        //*******check the order and partner is present or not***************
        if(ordersDb.containsKey(orderId) && partnersDb.containsKey(partnerId)) {
            //First add this pair in the Orders-Partners Map
            orderToPartnerDb.put(orderId, partnerId);

            //Now Add this pair in the Partner-Orders Map
            List<String> currentOrders = new ArrayList<>();

            if (partnerToOrderDb.containsKey(partnerId))
                currentOrders = partnerToOrderDb.get(partnerId);

            currentOrders.add(orderId);
            partnerToOrderDb.put(partnerId, currentOrders);

            //now update the number of order count of that particular partner
            DeliveryPartner deliveryPartner = partnersDb.get(partnerId);
            deliveryPartner.setNumberOfOrders(currentOrders.size());
        }
    }

    public Order getOrderById(String orderId) {
        return ordersDb.get(orderId);
    }

    public DeliveryPartner getPartnerById(String partnerId) {
        return partnersDb.get(partnerId);
    }

    public Integer getOrderCountByPartnerId(String partnerId) {
        if(!partnersDb.containsKey(partnerId) || !partnerToOrderDb.containsKey(partnerId))
            return 0;
        return partnerToOrderDb.get(partnerId).size();

        //return partnersMap.get(partnerId).getNumberOfOrders();
    }

    public List<String> getOrdersByPartnerId(String partnerId) {
        return partnerToOrderDb.get(partnerId);
    }

    public List<String> getAllOrders() {
        List<String> list = new ArrayList<>();
        for(String order : ordersDb.keySet()){
            list.add(order);
        }
        return list;
    }

    public Integer getCountOfUnassignedOrders() {
        return ordersDb.size() - orderToPartnerDb.size();
    }

    public int getLastDeliveryTimeByPartnerId(String partnerId) {
        if(!partnerToOrderDb.containsKey(partnerId))
            return -1;
        List<String> list = partnerToOrderDb.get(partnerId);
        int maxTime = 0;
        for(String orderId : list){
            int currTime = ordersDb.get(orderId).getDeliveryTime();
            maxTime = Math.max(maxTime, currTime);
        }
        return maxTime;
    }

    public Integer getOrdersLeftAfterGivenTimeByPartnerId(int givenTime, String partnerId) {
        List<String> list = partnerToOrderDb.get(partnerId);
        int count = 0;
        for(String orderId : list){
            if(ordersDb.get(orderId).getDeliveryTime() > givenTime)
                count++;
        }
        return Integer.valueOf(count);
    }

    public void deletePartnerById(String partnerId) {
        if(partnersDb.containsKey(partnerId)) {
            if (partnersDb.get(partnerId).getNumberOfOrders() > 0) {
                List<String> orders = partnerToOrderDb.get(partnerId);

                //Remove from Partner-Order Map
                partnerToOrderDb.remove(partnerId);

                //Remove from Order-Partner Map
                for (String order : orders) {
                    orderToPartnerDb.remove(order);
                }
            }

            //Remove from Partner Map
            partnersDb.remove(partnerId);
        }
    }

    public void deleteOrderById(String orderId) {
        if(ordersDb.containsKey(orderId)) {
            String partnerId = orderToPartnerDb.get(orderId);
            if (partnerId != null) {
                //Remove from Order-Partner Map
                orderToPartnerDb.remove(orderId);

                //Remove form Partner-Order Map
                partnerToOrderDb.get(partnerId).remove(orderId);
                int currOrder = partnersDb.get(partnerId).getNumberOfOrders();
                partnersDb.get(partnerId).setNumberOfOrders(currOrder - 1);

            }

            //Remove from Orders Map
            ordersDb.remove(orderId);
        }
    }
}