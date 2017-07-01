package org.axisgroup.base.controller;
import org.axisgroup.common.dto.Account;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {  
public static void main(String[] args) {  
      
    //creating configuration object  
    Configuration cfg=new Configuration();  
    System.out.println();
    cfg.configure("hibernate.cfg.xml");//populates the data of the configuration file  
      
    //creating seession factory object  
    SessionFactory factory=cfg.buildSessionFactory();  
      
    //creating session object  
    Session session=factory.openSession();  
      
    //creating transaction object  
    Transaction t=session.beginTransaction();  
          
    ApplicationContext context =
      	  new ClassPathXmlApplicationContext(new String[] {"dispatcher-servlet.xml"});

      	Account cust = (Account)context.getBean("AccountBean");
      	cust.getAccountService().setAddress("123 st");
      	cust.setPersonalAccount("new account");
      	cust.getAccountService().setName("New Name");
      	System.out.println(cust.getPersonalAccount());

   
    
    session.persist(cust);
      
    t.commit();//transaction is committed  
    session.close();  
    
    factory.close();
      
    System.out.println("successfully saved");  
      
}  
}  