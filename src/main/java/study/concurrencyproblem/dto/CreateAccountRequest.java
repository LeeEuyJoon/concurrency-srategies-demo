package study.concurrencyproblem.dto;

public class CreateAccountRequest {
    private Integer initialBalance;
    
    // 기본 생성자
    public CreateAccountRequest() {}
    
    // 생성자
    public CreateAccountRequest(Integer initialBalance) {
        this.initialBalance = initialBalance;
    }
    
    // Getter, Setter
    public Integer getInitialBalance() {
        return initialBalance;
    }
    
    public void setInitialBalance(Integer initialBalance) {
        this.initialBalance = initialBalance;
    }
}