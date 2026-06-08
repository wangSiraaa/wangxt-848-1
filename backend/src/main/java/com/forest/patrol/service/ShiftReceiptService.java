package com.forest.patrol.service;

import com.forest.patrol.common.BusinessException;
import com.forest.patrol.dto.ShiftReceiptDTO;
import com.forest.patrol.entity.*;
import com.forest.patrol.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftReceiptService {

    private final ShiftReceiptRepository shiftReceiptRepository;
    private final WorkShiftRepository workShiftRepository;
    private final ShiftUserRepository shiftUserRepository;
    private final UserRepository userRepository;
    private final PatrolRouteRepository patrolRouteRepository;
    private final CheckInRecordRepository checkInRecordRepository;

    public List<ShiftReceipt> findAll() {
        return shiftReceiptRepository.findAll();
    }

    public Optional<ShiftReceipt> findById(Long id) {
        return shiftReceiptRepository.findById(id);
    }

    public List<ShiftReceipt> findByShiftId(Long shiftId) {
        return shiftReceiptRepository.findByShiftIdOrderByCreatedAtDesc(shiftId);
    }

    public List<ShiftReceipt> findByUserId(Long userId) {
        return shiftReceiptRepository.findByUserId(userId);
    }

    public Optional<ShiftReceipt> findByShiftIdAndUserId(Long shiftId, Long userId) {
        return shiftReceiptRepository.findByShiftIdAndUserId(shiftId, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public ShiftReceipt createReceipt(ShiftReceiptDTO dto) {
        log.info("创建处理回执: shiftId={}, userId={}, handlerId={}",
                dto.getShiftId(), dto.getUserId(), dto.getHandlerId());

        validateReceiptCreate(dto);

        WorkShift shift = workShiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new BusinessException("班次不存在"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        User handler = userRepository.findById(dto.getHandlerId())
                .orElseThrow(() -> new BusinessException("处理人不存在"));

        ShiftUser shiftUser = shiftUserRepository.findByShiftIdAndUserId(dto.getShiftId(), dto.getUserId())
                .orElseThrow(() -> new BusinessException("用户不在该班次中"));

        if (shiftReceiptRepository.existsByShiftIdAndUserId(dto.getShiftId(), dto.getUserId())) {
            throw new BusinessException("该班次用户已存在处理回执");
        }

        PatrolRoute route = patrolRouteRepository.findById(shift.getRouteId()).orElse(null);

        Integer totalCheckpoints = dto.getTotalCheckpoints();
        if (totalCheckpoints == null || totalCheckpoints == 0) {
            totalCheckpoints = (route != null && route.getCheckpoints() != null) ? route.getCheckpoints() : 0;
        }

        Integer completedCheckpoints = dto.getCompletedCheckpoints();
        if (completedCheckpoints == null) {
            completedCheckpoints = checkInRecordRepository.countDistinctCheckpointsByShiftIdAndUserId(
                    dto.getShiftId(), dto.getUserId());
            if (completedCheckpoints == null) completedCheckpoints = 0;
        }

        BigDecimal completionRate = dto.getCompletionRate();
        if (completionRate == null && totalCheckpoints > 0) {
            completionRate = BigDecimal.valueOf(completedCheckpoints)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCheckpoints), 2, RoundingMode.HALF_UP);
        } else if (completionRate == null) {
            completionRate = BigDecimal.ZERO;
        }

        String routeCompletion = dto.getRouteCompletion();
        if (routeCompletion == null) {
            if (totalCheckpoints == 0) {
                routeCompletion = ShiftReceipt.RouteCompletion.NOT_STARTED;
            } else if (completedCheckpoints >= totalCheckpoints) {
                routeCompletion = ShiftReceipt.RouteCompletion.COMPLETED;
            } else if (completedCheckpoints > 0) {
                routeCompletion = ShiftReceipt.RouteCompletion.PARTIAL;
            } else {
                routeCompletion = ShiftReceipt.RouteCompletion.NOT_STARTED;
            }
        }

        ShiftReceipt receipt = new ShiftReceipt();
        receipt.setShiftId(dto.getShiftId());
        receipt.setUserId(dto.getUserId());
        receipt.setHandlerId(dto.getHandlerId());
        receipt.setRouteCompletion(routeCompletion);
        receipt.setTotalCheckpoints(totalCheckpoints);
        receipt.setCompletedCheckpoints(completedCheckpoints);
        receipt.setCompletionRate(completionRate);
        receipt.setSignInTime(dto.getSignInTime() != null ? dto.getSignInTime() : shiftUser.getSignInTime());
        receipt.setSignOutTime(dto.getSignOutTime() != null ? dto.getSignOutTime() : shiftUser.getSignOutTime());
        receipt.setHandleRemark(dto.getHandleRemark());

        receipt = shiftReceiptRepository.save(receipt);

        log.info("处理回执创建成功: receiptId={}, shiftId={}, userId={}, completionRate={}",
                receipt.getId(), receipt.getShiftId(), receipt.getUserId(), receipt.getCompletionRate());

        return receipt;
    }

    private void validateReceiptCreate(ShiftReceiptDTO dto) {
        if (dto.getShiftId() == null) {
            throw new BusinessException("班次ID不能为空");
        }
        if (dto.getUserId() == null) {
            throw new BusinessException("人员ID不能为空");
        }
        if (dto.getHandlerId() == null) {
            throw new BusinessException("处理人ID不能为空");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ShiftReceipt updateReceipt(Long id, ShiftReceiptDTO dto) {
        log.info("更新处理回执: id={}", id);

        ShiftReceipt receipt = shiftReceiptRepository.findById(id)
                .orElseThrow(() -> new BusinessException("处理回执不存在"));

        if (dto.getRouteCompletion() != null) {
            receipt.setRouteCompletion(dto.getRouteCompletion());
        }
        if (dto.getTotalCheckpoints() != null) {
            receipt.setTotalCheckpoints(dto.getTotalCheckpoints());
        }
        if (dto.getCompletedCheckpoints() != null) {
            receipt.setCompletedCheckpoints(dto.getCompletedCheckpoints());
        }
        if (dto.getCompletionRate() != null) {
            receipt.setCompletionRate(dto.getCompletionRate());
        }
        if (dto.getSignInTime() != null) {
            receipt.setSignInTime(dto.getSignInTime());
        }
        if (dto.getSignOutTime() != null) {
            receipt.setSignOutTime(dto.getSignOutTime());
        }
        if (dto.getHandleRemark() != null) {
            receipt.setHandleRemark(dto.getHandleRemark());
        }

        return shiftReceiptRepository.save(receipt);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReceipt(Long id) {
        ShiftReceipt receipt = shiftReceiptRepository.findById(id)
                .orElseThrow(() -> new BusinessException("处理回执不存在"));
        shiftReceiptRepository.delete(receipt);
        log.info("处理回执删除成功: id={}", id);
    }

    @Transactional(rollbackFor = Exception.class)
    public ShiftReceipt createReceiptWithCheck(Long shiftId, Long userId, Long handlerId, String handleRemark) {
        ShiftReceiptDTO dto = new ShiftReceiptDTO();
        dto.setShiftId(shiftId);
        dto.setUserId(userId);
        dto.setHandlerId(handlerId);
        dto.setHandleRemark(handleRemark);
        return createReceipt(dto);
    }
}
