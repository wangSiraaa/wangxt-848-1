package com.forest.patrol.service;

import com.forest.patrol.common.BusinessException;
import com.forest.patrol.dto.CheckInDTO;
import com.forest.patrol.entity.CheckInRecord;
import com.forest.patrol.entity.ShiftUser;
import com.forest.patrol.entity.WorkShift;
import com.forest.patrol.repository.CheckInRecordRepository;
import com.forest.patrol.repository.ShiftUserRepository;
import com.forest.patrol.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRecordRepository checkInRecordRepository;
    private final WorkShiftRepository workShiftRepository;
    private final ShiftUserRepository shiftUserRepository;

    public List<CheckInRecord> findByShiftId(Long shiftId) {
        return checkInRecordRepository.findByShiftIdOrderByCheckpointOrder(shiftId);
    }

    public List<CheckInRecord> findByShiftIdAndUserId(Long shiftId, Long userId) {
        return checkInRecordRepository.findByShiftIdAndUserIdOrderByCheckpointOrder(shiftId, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public CheckInRecord checkIn(CheckInDTO dto) {
        WorkShift shift = workShiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new BusinessException("班次不存在"));

        if (!WorkShift.Status.IN_PROGRESS.equals(shift.getStatus())) {
            throw new BusinessException("班次未开始或已结束");
        }

        ShiftUser shiftUser = shiftUserRepository.findByShiftIdAndUserId(dto.getShiftId(), dto.getUserId())
                .orElseThrow(() -> new BusinessException("用户不在该班次中"));

        if (shiftUser.getSignInTime() == null) {
            throw new BusinessException("请先签到");
        }

        if (shiftUser.getSignOutTime() != null) {
            throw new BusinessException("已签退，无法打卡");
        }

        if (checkInRecordRepository.existsByShiftIdAndUserIdAndCheckpointOrder(
                dto.getShiftId(), dto.getUserId(), dto.getCheckpointOrder())) {
            throw new BusinessException("该打卡点已打卡");
        }

        CheckInRecord record = new CheckInRecord();
        record.setShiftId(dto.getShiftId());
        record.setUserId(dto.getUserId());
        record.setRouteId(shift.getRouteId());
        record.setCheckpointName(dto.getCheckpointName());
        record.setCheckpointOrder(dto.getCheckpointOrder());
        record.setCheckInTime(LocalDateTime.now());
        record.setLocation(dto.getLocation());
        record.setRemark(dto.getRemark());

        record = checkInRecordRepository.save(record);
        log.info("打卡成功: shiftId={}, userId={}, checkpoint={}",
                dto.getShiftId(), dto.getUserId(), dto.getCheckpointName());
        return record;
    }

    public Integer countCheckPoints(Long shiftId) {
        return checkInRecordRepository.countDistinctCheckpointsByShiftId(shiftId);
    }
}
