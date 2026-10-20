package com.campus.proposal.config;

import com.campus.proposal.common.ProposalStatus;
import com.campus.proposal.common.Role;
import com.campus.proposal.entity.Proposal;
import com.campus.proposal.entity.ProposalLog;
import com.campus.proposal.entity.User;
import com.campus.proposal.repository.ProposalLogRepository;
import com.campus.proposal.repository.ProposalRepository;
import com.campus.proposal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 演示种子数据:首次启动(用户表为空)时写入 5 个账号和 5 条处于不同阶段的提案,
 * 让演示一打开就有数据可看。仅在空库时执行,不覆盖已有数据。
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProposalRepository proposalRepository;
    private final ProposalLogRepository logRepository;

    public DataSeeder(UserRepository userRepository, ProposalRepository proposalRepository,
                      ProposalLogRepository logRepository) {
        this.userRepository = userRepository;
        this.proposalRepository = proposalRepository;
        this.logRepository = logRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User xiaoming = user("xiaoming", "张晓宇", Role.PROPOSER, null);
        User xiaohong = user("xiaohong", "陈雨桐", Role.PROPOSER, null);
        User staff = user("staff", "李静", Role.STAFF, "提案工作办公室");
        User houqin = user("houqin", "王建国", Role.DEPT, "后勤管理处");
        User jiaowu = user("jiaowu", "刘敏", Role.DEPT, "教务处");
        userRepository.saveAll(List.of(xiaoming, xiaohong, staff, houqin, jiaowu));

        LocalDateTime base = LocalDateTime.now().minusDays(6);
        int year = base.getYear();

        // 1. 已提交·待审查
        Proposal p1 = proposal("TA-" + year + "-0001", "学生活动中心排练房预约全靠微信群",
                "目前社团排练房预约由各社团在微信群里接龙,经常出现两个社团同一时段都到场的情况。\n"
                        + "建议提供线上预约入口,显示可预约时段并自动冲突检测。",
                xiaoming, null, ProposalStatus.SUBMITTED, base);

        // 2. 已立案·待分办
        Proposal p2 = proposal("TA-" + year + "-0002", "三教自习室晚间插座不足",
                "三号教学楼 302、305 自习室插座数量少,晚间很多同学笔记本没电只能提前离开。\n"
                        + "建议排查并加装地面插座或排插位。",
                xiaohong, null, ProposalStatus.ACCEPTED, base.plusDays(1));
        log(p2, "提交", xiaohong, null, base.plusDays(1));
        log(p2, "立案", staff, "情况属实,建议后勤部门实地查看", base.plusDays(2));
        p2.setUpdatedAt(base.plusDays(2));

        // 3. 办理中
        Proposal p3 = proposal("TA-" + year + "-0003", "一号食堂二楼高峰期排队过长",
                "11:40-12:10 一号食堂二楼只有 4 个窗口营业,排队经常超过 15 分钟。\n"
                        + "建议高峰期临时增开窗口或引导分流到三楼。",
                xiaoming, houqin, ProposalStatus.ASSIGNED, base.plusDays(2));
        log(p3, "提交", xiaoming, null, base.plusDays(2));
        log(p3, "立案", staff, "转后勤管理处核实", base.plusDays(2).plusHours(3));
        log(p3, "分办", staff, "后勤管理处·王建国", base.plusDays(3));
        p3.setUpdatedAt(base.plusDays(3));

        // 4. 已答复·待评价
        Proposal p4 = proposal("TA-" + year + "-0004", "图书馆闭馆时间能否延长至 23:00",
                "期末季图书馆 22:00 闭馆,很多同学希望能延长到 23:00,并保留夜间自习区。",
                xiaohong, jiaowu, ProposalStatus.REPLIED, base.plusDays(3));
        p4.setReply("已与图书馆协调:自下周一起闭馆时间延长至 23:00 试行一个月,一楼 104 自习区同步开放,试行结束后根据使用情况评估是否长期执行。");
        log(p4, "提交", xiaohong, null, base.plusDays(3));
        log(p4, "立案", staff, null, base.plusDays(3).plusHours(2));
        log(p4, "分办", staff, "教务处·刘敏", base.plusDays(4));
        log(p4, "答复", jiaowu, "下周一起试行延长闭馆", base.plusDays(5));
        p4.setUpdatedAt(base.plusDays(5));

        // 5. 已办结
        Proposal p5 = proposal("TA-" + year + "-0005", "操场夜间照明灯多盏损坏",
                "西区操场东侧跑道 6 盏照明灯不亮,晚跑存在安全隐患。",
                xiaoming, houqin, ProposalStatus.CLOSED, base.plusDays(1));
        p5.setReply("后勤已安排电工检修,损坏灯组于本周三全部更换完毕,并增设夜间巡检。");
        p5.setRating(5);
        p5.setEvaluation("三天就修好了,效率很高,点赞!");
        log(p5, "提交", xiaoming, null, base.plusDays(1));
        log(p5, "立案", staff, null, base.plusDays(1).plusHours(4));
        log(p5, "分办", staff, "后勤管理处·王建国", base.plusDays(2));
        log(p5, "答复", houqin, "本周三前完成检修", base.plusDays(3));
        log(p5, "评价", xiaoming, "满意度 5 星", base.plusDays(4));
        p5.setUpdatedAt(base.plusDays(4));

        proposalRepository.saveAll(List.of(p1, p2, p3, p4, p5));
        log(p1, "提交", xiaoming, null, base);
    }

    private User user(String username, String name, Role role, String department) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("123456"); // 演示账号统一密码
        u.setName(name);
        u.setRole(role);
        u.setDepartment(department);
        return u;
    }

    private Proposal proposal(String code, String title, String content, User proposer, User handler,
                              ProposalStatus status, LocalDateTime createdAt) {
        Proposal p = new Proposal();
        p.setCode(code);
        p.setTitle(title);
        p.setContent(content);
        p.setProposer(proposer);
        p.setHandler(handler);
        p.setStatus(status);
        p.setCreatedAt(createdAt);
        p.setUpdatedAt(createdAt);
        return p;
    }

    private void log(Proposal p, String action, User operator, String note, LocalDateTime time) {
        ProposalLog l = new ProposalLog();
        l.setProposal(p);
        l.setAction(action);
        l.setOperator(operator);
        l.setNote(note);
        l.setCreatedAt(time);
        logRepository.save(l);
    }
}
