/*
 * APDPlat - Application Product Development Platform
 * Copyright (c) 2013, 杨尚川, yang-shangchuan@qq.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.apdplat.superword.system;

import org.apdplat.superword.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 反爬虫反机器人攻击
 * Created by ysc on 12/4/15.
 */
public class AntiRobotFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(AntiRobotFilter.class);

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public static int limit = 1000;

    private static ServletContext servletContext = null;

    public void destroy() {
    }

    private String getKey(HttpServletRequest request){
        String host = request.getRemoteAddr();
        User user = (User) request.getSession().getAttribute("user");
        String userString = user==null?"anonymity":user.getUserName();
        LocalDateTime timePoint = LocalDateTime.now();
        return "anti-robot-"+userString+"-"+host+"-"+timePoint.getYear()+""+timePoint.getMonth().getValue()+""+timePoint.getDayOfMonth();
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest)req;
        if(servletContext == null){
            servletContext = request.getServletContext();
        }
        String key = getKey(request);
        AtomicInteger count = (AtomicInteger)servletContext.getAttribute(key);
        if(count == null){
            count = new AtomicInteger();
            servletContext.setAttribute(key, count);
        }

        if(count.incrementAndGet() > limit){
            HttpServletResponse response = (HttpServletResponse)resp;
            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write("系统检测到您所在的IP访问过于频繁，给您造成的不便敬请谅解，请明天再来。再见！");

            return;
        }

        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {
        int initialDelay = 24-LocalDateTime.now().getHour();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            LOG.info("clear last day anti-robot counter");
            LocalDateTime timePoint = LocalDateTime.now().minusDays(1);
            String date = timePoint.getYear() + "" + timePoint.getMonth().getValue() + "" + timePoint.getDayOfMonth();
            List<String> archive = new ArrayList<>();
            Enumeration<String> keys = servletContext.getAttributeNames();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                if (key.startsWith("anti-robot-") && key.endsWith(date)) {
                    servletContext.removeAttribute(key);
                    archive.add(key);
                }
            }
            try {
                File path = new File(servletContext.getRealPath("/WEB-INF/anti-robot-archive/"));
                if (!path.exists()) {
                    path.mkdirs();
                }
                String file = path.getPath() + date + ".txt";
                Files.write(Paths.get(file), archive);
                LOG.info("clear last day anti-robot counter finished: " + file);
            } catch (Exception e) {
                LOG.error("save anti-robot-archive failed", e);
            }
        }, initialDelay, 24, TimeUnit.HOURS);
    }

    public static List<String> getData(){
        Map<String, Integer> map = new HashMap<>();
        Enumeration<String> keys = servletContext.getAttributeNames();
        while(keys.hasMoreElements()){
            String key = keys.nextElement();
            if(key.startsWith("anti-robot-")){
                map.put(key.substring(11), ((AtomicInteger) servletContext.getAttribute(key)).intValue());
            }
        }
        return map
                .entrySet()
                .stream()
                .sorted((a,b)->b.getValue().compareTo(a.getValue()))
                .map(e->e.getKey()+"-"+e.getValue())
                .collect(Collectors.toList());
    }
}