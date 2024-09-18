package com.example.demo.services;

import com.example.demo.entities.Customer;

import java.util.List;

public interface CustomerService {

    List<Customer> getAllCustomers();

    Customer getCustomerByName(String name);
}
