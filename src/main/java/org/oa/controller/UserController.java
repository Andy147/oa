package org.oa.controller;

import com.sun.deploy.net.HttpResponse;
import net.sf.json.JSONObject;
import org.oa.model.User;
import org.oa.service.IUserServer;
import org.oa.utils.MyVerificationCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    IUserServer userServer;
    @Autowired
    HttpSession session;
    private String verification;//记录验证码
    @RequestMapping("startAction.do")
    public String execute()
    {
        System.out.println("进入登录页面。。。。");
        return "login";
    }
    @RequestMapping("getVerification.do")
    @ResponseBody
    public String getVerification(HttpServletRequest request, HttpServletResponse response)
    {

        verification = MyVerificationCode.getRamCode(6 , null);
        System.out.println("verification:" + verification);
        BufferedImage image = MyVerificationCode.getImageFromCode(verification ,
                80 , 32 , 5 , new Color(0xcccccc));
        OutputStream out = null;
        try {
            out = response.getOutputStream();
            ImageIO.write(image , "png" , out);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (out!=null)
                {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
    @RequestMapping(value = "login.do"  , produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String login(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        String userName = request.getParameter("name");
        String pwd =  request.getParameter("pwd");
        String code = request.getParameter("code");
        response.setContentType("application/json;charset=UTF-8");
        JSONObject reply = new JSONObject();
        if(userName != null && !"".equals(userName) && pwd !=null && !"".equals(pwd))
        {
            //先判断验证码
            System.out.println("code:" + code +"==="+ "verification:" + verification);
            if (!code.toUpperCase().equals(verification.toUpperCase()))
            {
                reply.put("statu" , -1);
                reply.put("reason" , "验证码不正确");
                String str = reply.toString(2);
                return reply.toString(2);
            };
            //获取用户名和密码
            User user = userServer.getUserByName(userName , pwd);
            if (user != null)
            {
                reply.put("statu" , 0);
                reply.put("reason" , "OK");
                session.setAttribute("user" , user);
            }
            else
            {
                reply.put("statu" , -1);
                reply.put("reason" , "用户名或密码错误");
            }
        }
        return reply.toString(2);
    }
    @RequestMapping("index.do")
    public String turn2Index()
    {

        return "index";
    }

    /**
     * 登出
     * @return
     */
    @RequestMapping("logout.do")
    public String logout()
    {
        //先将session设置为空
        session.setAttribute("user" , null);
        return "redirect:startAction.do";
    }

}
