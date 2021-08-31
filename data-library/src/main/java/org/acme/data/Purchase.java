package org.acme.data;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

public class Purchase {

    private String customerId;
    private String item;
    private Integer quantity;
    private Double price;
    private String creditCardNumber;
    private String store;

    private Date transactionDate;
    private PurchaseKey purchaseKey;

    protected Purchase() {
        this.transactionDate = Date.from(Instant.now());
        this.purchaseKey = new PurchaseKey(this.transactionDate);
    }

    public Purchase(String id, String customerId, String creditCardNumber, String item,  String store, int quantity, double price) {
        this.item = item;
        this.quantity = quantity;
        this.price = price;
        this.customerId = customerId;
        this.creditCardNumber = creditCardNumber;
        this.store = store;
        this.transactionDate = Date.from(Instant.now());
        this.purchaseKey = new PurchaseKey(id, customerId, this.transactionDate);
    }

    public Purchase(String id, String customerId) {
        this.customerId = customerId;
        this.transactionDate = Date.from(Instant.now());
        this.purchaseKey = new PurchaseKey(id, customerId, this.transactionDate);
    }

    public PurchaseKey getPurchaseKey() {
        return purchaseKey;
    }

    public void setPurchaseKey(PurchaseKey purchaseKey) {
        this.purchaseKey = purchaseKey;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public static Builder builder(Purchase purchase) {
        return new Builder(purchase);
    }

    public static class Builder {
        private Purchase purchase;

        Builder(Purchase purchase) {
            purchase.setCreditCardNumber(maskCreditCard(purchase.getCreditCardNumber()));
            this.purchase = purchase;
        }

        private String maskCreditCard(String creditCardNumber) {
            final String CC_NUMBER_REPLACEMENT="xxxx-xxxx-xxxx-";
            String[] parts = creditCardNumber.split("-");
            if (parts.length < 4 ) {
                creditCardNumber = "xxxx";
            } else {
                String last4Digits = creditCardNumber.split("-")[3];
                creditCardNumber = CC_NUMBER_REPLACEMENT + last4Digits;
            }
            return creditCardNumber;
        }

        public Purchase build() {
            return this.purchase;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Purchase purchase = (Purchase) o;
        return Objects.equals(customerId, purchase.customerId) && Objects.equals(item, purchase.item) && Objects.equals(quantity, purchase.quantity) && Objects.equals(price, purchase.price) && Objects.equals(creditCardNumber, purchase.creditCardNumber) && Objects.equals(store, purchase.store) && Objects.equals(transactionDate, purchase.transactionDate) && Objects.equals(purchaseKey, purchase.purchaseKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, item, quantity, price, creditCardNumber, store, transactionDate, purchaseKey);
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "customerId='" + customerId + '\'' +
                ", item='" + item + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", creditCardNumber='" + creditCardNumber + '\'' +
                ", store='" + store + '\'' +
                ", transactionDate=" + transactionDate +
                ", purchaseKey=" + purchaseKey +
                '}';
    }
}
