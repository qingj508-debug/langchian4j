package com.wei.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wei.entity.RefundRecords;
import com.wei.entity.Tickets;
import com.wei.entity.Users;
import com.wei.mapper.RefundRecordsMapper;
import com.wei.mapper.TicketsMapper;
import com.wei.mapper.UsersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
    @Autowired
    TicketsMapper ticketMapper;
    @Autowired
    UsersMapper usersMapper;
    @Autowired
    RefundRecordsMapper refundRecordsMapper;

    public List<Tickets> findTicketByUserId(String username){
        Users users=usersMapper.selectOne(this.findUserIdByUserName(username));
        if (users == null) {
            return List.of();
        }
        QueryWrapper<Tickets> wrapper=new QueryWrapper<Tickets>();
        wrapper.eq("user_id",users.getUserId())
                .eq("is_refunded",false);
        return ticketMapper.selectList(wrapper);
    }


    public List<Tickets> findAllTicketByUserId(String username){
        Users users=usersMapper.selectOne(this.findUserIdByUserName(username));
        if (users == null) {
            return List.of();
        }
        QueryWrapper<Tickets> wrapper=new QueryWrapper<Tickets>();
        wrapper.eq("user_id",users.getUserId());
        return ticketMapper.selectList(wrapper);
    }


    public boolean refundTicket(String username,String ticketId){
        Users users=usersMapper.selectOne(this.findUserIdByUserName(username));
        if (users == null) {
            return false;
        }
        Tickets tickets=ticketMapper.selectById(ticketId);
        if (tickets != null && !tickets.getIsRefunded() && tickets.getUserId()==users.getUserId()) {
            RefundRecords refundRecords=new RefundRecords();
            refundRecords.setTicketId(tickets.getTicketId());
            refundRecords.setRefundTime(LocalDateTime.now());
            refundRecords.setRefundAmount(tickets.getPrice());
            refundRecordsMapper.insert(refundRecords);
            tickets.setIsRefunded(true);
            ticketMapper.updateById(tickets);
            return true;
        }

        return false;
    }

    public QueryWrapper<Users> findUserIdByUserName(String username){
        QueryWrapper<Users> wrapper=new QueryWrapper<Users>();
        wrapper.eq("username", username);
        return wrapper;
    }

}
