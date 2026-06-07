package com.forest.patrol.service;

import com.forest.patrol.common.BusinessException;
import com.forest.patrol.dto.ShiftCreateDTO;
import com.forest.patrol.dto.ShiftStatisticsVO;
import com.forest.patrol.dto.SignInOutDTO;
import com.forest.patrol.entity.PatrolRoute;
import com.forest.patrol.entity.ShiftUser;
import com.forest.patrol.entity.User;
import com.forest.patrol.entity.WorkShift;
import com.forest.patrol.repository.CheckInRecordRepository;
import com.forest.patrol.repository.PatrolRouteRepository;
import com.forest.patrol.repository.ShiftUserRepository;
import com.forest.patrol.repository.UserRepository;
import com.forest.patrol.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkShiftService {

    private final WorkShiftRepository workShiftRepository;
    private final ShiftUserRepository shiftUserRepository;
    private final UserRepository userRepository;
    private final PatrolRouteRepository patrolRouteRepository;
    private final CheckInRecordRepository checkInRecordRepository;

    public List<WorkShift> findAll() {
        return workShiftRepository.findAll();
    }

    public List<WorkShift> findByDate(LocalDate date) {
        return workShiftRepository.findByShiftDate(date);
    }

    public List<WorkShift> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return workShiftRepository.findByShiftDateBetween(startDate, endDate);
    }

    public List<WorkShift> findByUserId(Long userId) {
        return workShiftRepository.findByUserIdAndDateRange(userId, LocalDate.now().minusDays(30));
    }

    public Optional<WorkShift> findById(Long id) {
        return workShiftRepository.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkShift createShift(ShiftCreateDTO dto) {
        log.info("创建班次: date={}, type={}, fireRisk={}, users={}",
                dto.getShiftDate(), dto.getShiftType(), dto.getFireRiskLevel(), dto.getUserIds());

        validateShiftCreate(dto);

        PatrolRoute route = patrolRouteRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new BusinessException("巡护路线不存在"));

        WorkShift shift = new WorkShift();
        shift.setShiftDate(dto.getShiftDate());
        shift.setShiftType(dto.getShiftType());
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());
        shift.setFireRiskLevel(dto.getFireRiskLevel());
        shift.setRouteId(dto.getRouteId());
        shift.setStatus(WorkShift.Status.PENDING);

        shift = workShiftRepository.save(shift);

        List<User> users = userRepository.findAllById(dto.getUserIds());
        if (users.size() != dto.getUserIds().size()) {
            throw new BusinessException("部分用户不存在");
        }

        for (Long userId : dto.getUserIds()) {
            ShiftUser shiftUser = new ShiftUser();
            shiftUser.setShiftId(shift.getId());
            shiftUser.setUserId(userId);
            shiftUserRepository.save(shiftUser);
        }

        log.info("班次创建成功: shiftId={}", shift.getId());
        return shift;
    }

    private void validateShiftCreate(ShiftCreateDTO dto) {
        if (workShiftRepository.existsByShiftDateAndShiftType(dto.getShiftDate(), dto.getShiftType())) {
            throw new BusinessException("该日期该班次已存在排班");
        }

        if (WorkShift.FireRiskLevel.HIGH.equals(dto.getFireRiskLevel()) && dto.getUserIds().size() < 2) {
            throw new BusinessException("高火险日必须双人值守！请至少安排2名护林员");
        }

        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkShift updateShift(Long id, ShiftCreateDTO dto) {
        WorkShift shift = workShiftRepository.findById(id)
                .orElseThrow(() -> new BusinessException("班次不存在"));

        if (!WorkShift.Status.PENDING.equals(shift.getStatus())) {
            throw new BusinessException("只能修改待执行状态的班次");
        }

        if (WorkShift.FireRiskLevel.HIGH.equals(dto.getFireRiskLevel()) && dto.getUserIds().size() < 2) {
            throw new BusinessException("高火险日必须双人值守！请至少安排2名护林员");
        }

        shift.setShiftDate(dto.getShiftDate());
        shift.setShiftType(dto.getShiftType());
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());
        shift.setFireRiskLevel(dto.getFireRiskLevel());
        shift.setRouteId(dto.getRouteId());

        shiftUserRepository.deleteByShiftId(id);

        for (Long userId : dto.getUserIds()) {
            ShiftUser shiftUser = new ShiftUser();
            shiftUser.setShiftId(id);
            shiftUser.setUserId(userId);
            shiftUserRepository.save(shiftUser);
        }

        return workShiftRepository.save(shift);
    }

    @Transactional(rollbackFor = Exception.class)
    public void signIn(SignInOutDTO dto) {
        WorkShift shift = workShiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new BusinessException("班次不存在"));

        ShiftUser shiftUser = shiftUserRepository.findByShiftIdAndUserId(dto.getShiftId(), dto.getUserId())
                .orElseThrow(() -> new BusinessException("用户不在该班次中"));

        if (shiftUser.getSignInTime() != null) {
            throw new BusinessException("已签到，请勿重复签到");
        }

        LocalDateTime now = LocalDateTime.now();
        shiftUserRepository.updateSignInTime(dto.getShiftId(), dto.getUserId(), now);

        if (WorkShift.Status.PENDING.equals(shift.getStatus())) {
            shift.setStatus(WorkShift.Status.IN_PROGRESS);
            shift.setSignInTime(now);
            workShiftRepository.save(shift);
        }

        log.info("签到成功: shiftId={}, userId={}", dto.getShiftId(), dto.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void signOut(SignInOutDTO dto) {
        WorkShift shift = workShiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new BusinessException("班次不存在"));

        ShiftUser shiftUser = shiftUserRepository.findByShiftIdAndUserId(dto.getShiftId(), dto.getUserId())
                .orElseThrow(() -> new BusinessException("用户不在该班次中"));

        if (shiftUser.getSignInTime() == null) {
            throw new BusinessException("请先签到");
        }

        if (shiftUser.getSignOutTime() != null) {
            throw new BusinessException("已签退，请勿重复签退");
        }

        PatrolRoute route = patrolRouteRepository.findById(shift.getRouteId()).orElse(null);
        if (route != null && route.getCheckpoints() != null && route.getCheckpoints() > 0) {
            Integer completed = checkInRecordRepository.countDistinctCheckpointsByShiftIdAndUserId(dto.getShiftId(), dto.getUserId());
            if (completed == null) completed = 0;
            if (completed < route.getCheckpoints()) {
                throw new BusinessException("路线未完成，不能签退！已完成 " + completed + "/" + route.getCheckpoints() + " 个打卡点");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        shiftUserRepository.updateSignOutTime(dto.getShiftId(), dto.getUserId(), now);

        Integer totalUsers = workShiftRepository.countUsersByShiftId(dto.getShiftId());
        Integer signedOutUsers = shiftUserRepository.countSignedOutUsers(dto.getShiftId());
        if (signedOutUsers != null && totalUsers != null && signedOutUsers >= totalUsers) {
            shift.setStatus(WorkShift.Status.COMPLETED);
            shift.setSignOutTime(now);
            workShiftRepository.save(shift);
        }

        log.info("签退成功: shiftId={}, userId={}", dto.getShiftId(), dto.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelShift(Long id) {
        WorkShift shift = workShiftRepository.findById(id)
                .orElseThrow(() -> new BusinessException("班次不存在"));

        if (WorkShift.Status.IN_PROGRESS.equals(shift.getStatus())) {
            throw new BusinessException("进行中的班次不能取消");
        }

        shift.setStatus(WorkShift.Status.CANCELLED);
        workShiftRepository.save(shift);
    }

    public ShiftStatisticsVO getShiftStatistics(Long shiftId) {
        WorkShift shift = workShiftRepository.findById(shiftId)
                .orElseThrow(() -> new BusinessException("班次不存在"));

        PatrolRoute route = patrolRouteRepository.findById(shift.getRouteId()).orElse(null);

        ShiftStatisticsVO vo = new ShiftStatisticsVO();
        vo.setShiftId(shiftId);
        vo.setStatus(shift.getStatus());

        if (route != null) {
            vo.setRouteName(route.getRouteName());
            vo.setTotalCheckpoints(route.getCheckpoints());
        } else {
            vo.setTotalCheckpoints(0);
        }

        Integer completed = checkInRecordRepository.countDistinctCheckpointsByShiftId(shiftId);
        vo.setCompletedCheckpoints(completed == null ? 0 : completed);

        if (vo.getTotalCheckpoints() > 0) {
            BigDecimal rate = BigDecimal.valueOf(vo.getCompletedCheckpoints())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(vo.getTotalCheckpoints()), 2, RoundingMode.HALF_UP);
            vo.setCompletionRate(rate);
        } else {
            vo.setCompletionRate(BigDecimal.ZERO);
        }

        return vo;
    }

    public List<ShiftUser> getShiftUsers(Long shiftId) {
        return shiftUserRepository.findByShiftId(shiftId);
    }
}
