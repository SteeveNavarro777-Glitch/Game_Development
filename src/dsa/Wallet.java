/* FILE PURPOSE: COIN WALLET | Encapsulated balance and validated smith-shop transactions.
 * Course examples: encapsulation, integer invariants and transactional methods; docs/CODE_GUIDE.md.
 */
/** Owns Agyu's coins so collection and purchases use one authoritative balance. */
public final class Wallet {
    private int balance;

    // [BLOCK: ENCAPSULATION] Wallet.balance - Expose the balance without exposing mutable storage.
    public int balance(){return balance;}

    // [BLOCK: VALIDATED CREDIT] Wallet.deposit - Add a positive reward and reject integer overflow.
    public void deposit(int amount){
        if(amount<=0)throw new IllegalArgumentException("Coin deposit must be positive");
        balance=Math.addExact(balance,amount);
    }

    // [BLOCK: ATOMIC TRANSACTION] Wallet.spend - Change the balance only when the complete price is affordable.
    public boolean spend(int price){
        if(price<=0)throw new IllegalArgumentException("Coin price must be positive");
        if(balance<price)return false;
        balance-=price;return true;
    }
}
