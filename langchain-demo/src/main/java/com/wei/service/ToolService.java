package com.wei.service;

import com.wei.entity.Tickets;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ToolService {

    @Tool("广州今天的天气如何？")
    public String getWeather(@P("city") String city){
        System.out.println(city);
        return "广州今天的天气是晴天,26度";
    }

    @Autowired
    private TicketService ticketService;

//    @Tool("根据用户名查询用户的所有火车票")
//    public List<Tickets> findAllTicket(@P("username") String username){
//        return ticketService.findAllTicketByUserId(username);
//    }

    @Tool("根据用户名查询用户的所有未退票的火车票")
    public List<Tickets> findTicket(@P("username") String username){
        return ticketService.findTicketByUserId(username);
    }

    @Tool("根据用户名和票号进行退票")
    public boolean refundTicket(@P("username") String username,@P("ticketId") String ticketId){
        System.out.println(username);
        System.out.println(ticketId);
        return ticketService.refundTicket(username,ticketId);
    }
}
