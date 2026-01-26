package pl.btsoftware.backend.transfer.api.dto;

import java.util.List;

public record TransfersView(List<TransferView> transfers) {
    public TransfersView {
        transfers = List.copyOf(transfers);
    }
}
