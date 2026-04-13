package app.dearobjet.backend.domain.contract;

import app.dearobjet.backend.domain.contract.dto.ContractApplicationCountResponse;
import app.dearobjet.backend.domain.user.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ShopRepository shopRepository;

    @Transactional(readOnly = true)
    public ContractApplicationCountResponse getPendingApplicationCount(Long userId) {
        shopRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found for user: " + userId));

        long applicationCount = contractRepository.countPendingApplicationsByShopUserId(userId);
        List<String> artistNames = contractRepository.findPendingApplicationArtistNamesByShopUserId(userId);
        return new ContractApplicationCountResponse(applicationCount, artistNames);
    }
}
