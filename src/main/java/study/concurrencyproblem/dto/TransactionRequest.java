package study.concurrencyproblem.dto;

public class TransactionRequest {
    private Integer amount;
    
    public TransactionRequest() {}
    
    public TransactionRequest(Integer amount) {
        this.amount = amount;
    }
    
    public Integer getAmount() {
        return amount;
    }
    
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}