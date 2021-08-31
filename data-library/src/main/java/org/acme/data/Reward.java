package org.acme.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Reward {

    private String customerId;
    private Integer points;
    private Double spend;
    private Integer totalPoints;
    private Integer timeSinceLastPurchase;

    public Reward(Purchase purchase) {
        this.customerId = purchase.getCustomerId();
        BigDecimal s = new BigDecimal(purchase.getPrice() * purchase.getQuantity()).setScale(2, RoundingMode.HALF_UP);
        this.spend = s.doubleValue();
        BigDecimal p = new BigDecimal(this.spend).setScale(0, RoundingMode.HALF_UP);
        this.points = p.intValue() * 10;
        this.totalPoints = this.points;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Double getSpend() {
        return spend;
    }

    public void setSpend(Double spend) {
        this.spend = spend;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getTimeSinceLastPurchase() {
        return timeSinceLastPurchase;
    }

    public void setTimeSinceLastPurchase(Integer timeSinceLastPurchase) {
        this.timeSinceLastPurchase = timeSinceLastPurchase;
    }

    public void addRewardPoints(Integer previousTotalPoints) {
        this.totalPoints += previousTotalPoints;
    }

    public static Builder builder(Purchase purchase) {
        return new Builder(purchase);
    }

    public static class Builder {
        private Reward reward;

        Builder(Purchase purchase) {
            this.reward = new Reward(purchase);
        }

        public Reward build() {
            return this.reward;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reward reward = (Reward) o;
        return Objects.equals(customerId, reward.customerId) && Objects.equals(points, reward.points) && Objects.equals(spend, reward.spend) && Objects.equals(totalPoints, reward.totalPoints) && Objects.equals(timeSinceLastPurchase, reward.timeSinceLastPurchase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, points, spend, totalPoints, timeSinceLastPurchase);
    }

    @Override
    public String toString() {
        return "Reward{" +
                "customerId='" + customerId + '\'' +
                ", points=" + points +
                ", spend=" + spend +
                ", totalPoints=" + totalPoints +
                ", timeSinceLastPurchase=" + timeSinceLastPurchase +
                '}';
    }


}
