package org.acme.data;

import java.util.Date;
import java.util.Objects;

/*
    Composite Key
 */
public class PurchaseKey {
    private String id;
    private String customerId;
    private Date transactionDate;

    public PurchaseKey(String id, String customerId, Date transactionDate) {
        this.id = id;
        this.customerId = customerId;
        this.transactionDate = transactionDate;
    }

    public PurchaseKey(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseKey that = (PurchaseKey) o;
        return Objects.equals(id, that.id) && Objects.equals(customerId, that.customerId) && Objects.equals(transactionDate, that.transactionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerId, transactionDate);
    }

    @Override
    public String toString() {
        return "PurchaseKey{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
