package ru.alenavir.mini_ecommerce.entity.enums;

public enum OrderStatus {

    PROCESSING {
        @Override
        public boolean canChangeTo(OrderStatus next) {
            return next == PAID || next == CANCELLED;
        }
    },

    PAID {
        @Override
        public boolean canChangeTo(OrderStatus next) {
            return next == SHIPPED || next == CANCELLED;
        }
    },

    SHIPPED {
        @Override
        public boolean canChangeTo(OrderStatus next) {
            return next == DELIVERED;
        }
    },

    DELIVERED {
        @Override
        public boolean canChangeTo(OrderStatus next) {
            return false; // после доставки нельзя менять статус
        }
    },

    CANCELLED {
        @Override
        public boolean canChangeTo(OrderStatus next) {
            return false; // отмененный заказ нельзя изменить
        }
    };

    // Абстрактный метод — каждый статус должен его реализовать
    public abstract boolean canChangeTo(OrderStatus next);
}