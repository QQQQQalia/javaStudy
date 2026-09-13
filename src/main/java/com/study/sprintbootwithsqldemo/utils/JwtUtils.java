package com.study.sprintbootwithsqldemo.utils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

//token的生成和验证都调用这个类的方法
@Component
public class JwtUtils {
    //签名-》他一定要对于每个用户都是固定的，除非你重启或者特已修改。否则签名一至不变
    //一定要作为静态属性储存，这样才能保证不会每个用户登录都生成新的签名

    //1,借助第三方库自动的生成签名（每次重启java服务，都会重置签名，这就意味着，之前登录的用户token都失效了）
    // private static Key sign= Keys.secretKeyFor(SignatureAlgorithm.HS256); 
    //2,自己准备一个最少32位的字符串，然后用Keys的方法去变成签名
    //他不会随着重启而变化，除非你修改了signStr为别的字符串；
    private static String signStr="asdweqewrhsawe1_+a1sewqasdsaqwe2";
    private static Key sign=Keys.hmacShaKeyFor(signStr.getBytes(StandardCharsets.UTF_8));
    public String getToken(String id){
        //生成token
        //得到了一个Jwtbuilder对象,默认已经带好了头部了。

        //准备一个用来做payload的map对象
        Map<String,String> map=new HashMap<>();
        map.put("a","123");
        map.put("id",id);

        String token=Jwts.builder()
        // .setHeaderParam("alg", "ES256");改头部算法
        .setHeaderParam("test", "asd123")//往头部赛一个test属性-此时头部长这样-》{"alg":"HS256","typ":"JWT",test:asd123""}
        //1，你直接构建一个map对象，作为整个荷载加给jwtBuilder
        //2,你也可以给一个空的map对象给setClaims，然后通过setSubject和setExpiration一个个加
        .setClaims(map)//到这一步，等于荷载长这样-》{a:123,id:"!2312"}
        .setSubject(id)//设置唯一标识，等于荷载长这样-》{a:123,id:"!2312",sub:"!2312"}
        .setExpiration(new Date(System.currentTimeMillis()+24*3600*100))//设置过期时间,等于荷载长这样-》{a:123,id:"!2312",sub:"!2312",exp:18723232}
        .signWith(sign)//加入签名，到这一步位置，头部，荷载，签名都已经加入了。可以开始生产了
        .compact();//最终生成token
        return token;
    }

    public boolean verifyToken(String token){
        try{
            Jws<Claims> claims= Jwts.parserBuilder()//得到一个token解析对象
            .setSigningKey(sign)//给如签名
            .build() //得到了一个解析后的token对象
            .parseClaimsJws(token);

            Claims payload= claims.getBody();//取出荷载
            Header head=claims.getHeader();//取出头部
            System.out.println("body内容："+payload.toString());
            System.out.println("head内容："+head.toString());

            //判断是否过期
            if(payload.getExpiration().before(new Date())){
                return false;
            };
            //取出id来
            // payload.get("a")//取出payload里的a属性
            // payload.get("id")//取出id属性
            String id=payload.getSubject();
            //如果你想的话可以拿id再去查查数据
            return true;
        }catch(Exception err){
            return false;
        }
 
    }
}
 