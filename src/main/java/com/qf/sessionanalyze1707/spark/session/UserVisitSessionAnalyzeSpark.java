package com.qf.sessionanalyze1707.spark.session;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qf.sessionanalyze1707.constant.Constants;
import com.qf.sessionanalyze1707.dao.*;
import com.qf.sessionanalyze1707.dao.factory.DAOFactory;
import com.qf.sessionanalyze1707.domain.*;
import com.qf.sessionanalyze1707.util.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;

import java.util.*;

/**
 * 获取用户访问session数据进行分析
 * 1、接收使用者创建的任务信息
 * 任务中的过滤条件有：
 * 时间范围：起始时间-结束时间
 * 年龄范围
 * 性别
 * 职业
 * 所在城市
 * 用户搜索的关键字
 * 点击品类
 * 点击商品
 * 2、spark作业是如何接收使用者创建的任务信息
 * 1）shell脚本通知-调用spark-submit脚本
 * 2）从mysql的task表中根据指定的taskId来获取任务信息
 * 3、spark作业开始数据分析
 */
public class UserVisitSessionAnalyzeSpark {
    public static void main(String[] args) {

        /*模板代码*/
        // 创建配置信息类
        SparkConf conf = new SparkConf()
                .setAppName(Constants.SPARK_APP_NAME_SESSION);
        SparkUtils.setMaster(conf);
        // 创建集群入口类
        JavaSparkContext sc = new JavaSparkContext(conf);
        // SparkSql的上下文对象
        SQLContext sqlContext = SparkUtils.getSQLContext(sc.sc());

        // 设置检查点
//        sc.checkpointFile("hdfs://node01:9000/.....");

        // 生成模拟数据
        SparkUtils.mockData(sc, sqlContext);

        // 创建获取任务信息的实例
        ITaskDAO taskDAO = DAOFactory.getTaskDAO();
        // 获取指定的任务，需要拿到taskId
        Long taskId = ParamUtils.getTaskIdFromArgs(args, Constants.SPARK_LOCAL_TASKID_SESSION);

        // 根据taskId获取任务信息
        Task task = taskDAO.findById(taskId);

        if (task == null) {
            System.out.println(new Date() + "亲，你给的taskId我并不能获取到信息哦~");
        }

        // 根据task去task_param字段去获取对应的任务信息
        // task_param字段里存的就是使用者提供的查询条件
        JSONObject taskParam = JSON.parseObject(task.getTaskParam());

        // 开始查询指定日期范围内的行为数据（点击、搜索、下单、支付）
        // 首先要从user_visit_action这张hive表中查询出指定日期范围内的行为数据
        JavaRDD<Row> actionRDD = SparkUtils.getActionRDDByDateRange(sqlContext, taskParam);

        sc.stop();
    }

}
