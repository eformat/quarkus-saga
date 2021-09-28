all:
	make -C narayana-lra-coordinator podman-push
	make -C payment-service podman-push
	make -C hotel-service podman-push
	make -C flight-service podman-push
	make -C booking-service podman-push
