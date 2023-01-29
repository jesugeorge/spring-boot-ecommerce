package com.luv2code.ecommerce.service;

import com.luv2code.ecommerce.dao.CustomerRepository;
import com.luv2code.ecommerce.dto.Purchase;
import com.luv2code.ecommerce.dto.PurchaseResponse;
import com.luv2code.ecommerce.entity.Address;
import com.luv2code.ecommerce.entity.Customer;
import com.luv2code.ecommerce.entity.Order;
import com.luv2code.ecommerce.entity.OrderItem;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private CustomerRepository customerRepository;

    public CheckoutServiceImpl(CustomerRepository customerRepository){
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public PurchaseResponse placeOrder(Purchase purchase) {

        //retrieve order info from dto
        Order order = purchase.getOrder();

        // generate tracking number
        String trackingNumber = generateOrderTrackingNumber();
        order.setOrderTakingNumber(trackingNumber);

        // populate order with order items
        Set<OrderItem> orderItems = purchase.getOrderItems();
        orderItems.forEach(item -> order.add(item));

        //populate order with billing address and shipping address
        order.setBillingAddress(purchase.getBillingAddress());
        order.setShippingAddress(purchase.getShippingAddress());
        Address shippingAddress = purchase.getShippingAddress();

        //opulate customer with the order
        Customer customer = purchase.getCustomer(); ;

        String theEmail = customer.getEmail();

        Customer customerFromDB = customerRepository.findByEmail(theEmail);

        if(customerFromDB != null){
            customer = customerFromDB;
        }


        customer.add(order);

        //save to database
        customerRepository.save(customer);


        // return a response
        return new PurchaseResponse(trackingNumber);
    }

    private String generateOrderTrackingNumber() {

        //generate a random UUID number (UUID version-4)
        return UUID.randomUUID().toString();
    }
}
