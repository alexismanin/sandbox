package fr.amanin.stackoverflow;

import java.util.logging.Logger;
import reactor.core.publisher.Mono;

/**
 * Response for https://stackoverflow.com/questions/66884863
 */
public class ReactorZipServiceResults {

    static final Logger LOGGER = Logger.getLogger("");

    static final UserServiceApi userApi = new UserServiceApi();
    static final OrderServiceApi orderApi = new OrderServiceApi();
    static final VoucherServiceApi voucherApi = new VoucherServiceApi();

    public static void test(String userId) throws InterruptedException {
        final PseudoTimer timer = new PseudoTimer();
        // First, sign up user. Cache will cause any caller to get back result of any previous call on this Mono
        final Mono<UserDetailsResponse> userDetails = userApi.getUserDetails(userId)
                .doOnNext(next -> LOGGER.info("Queried user details at "+timer.time()))
                .cache();

        // Separately define pipeline branch that queries orders.
        final Mono<OrdersResponse> userOrders = userDetails
                .filter(it -> it.isSignupComplete())
                .flatMap(it -> orderApi.getOrdersByUserId(userId))
                .doOnNext(next -> LOGGER.info("Queried orders at "+timer.time()))
                .cache();

        // Pipeline branch serving for vouchers
        final Mono<VouchersResponse> userVouchers = userDetails
                .filter(it -> it.isVerified())
                .flatMap(it -> voucherApi.getVouchers(userId))
                .doOnNext(next -> LOGGER.info("Queried vouchers at "+timer.time()))
                .cache();

        // Prefetch orders and vouchers concurrently, so any following call will just get back cached values.
        // Note that it will also trigger user detail query (as they're derived from it)
        userOrders.subscribe();
        userVouchers.subscribe();

        // exagerate time lapse between request triggering and result assembly.
        Thread.sleep(20);

        LOGGER.info("Pipeline assembly at "+timer.time());
        // Assemble entire pipeline by concatenating all intermediate results sequentially
        userDetails
                .map(details -> new UserProfile(userId).details(details))
                .flatMap(profile -> userOrders.map(profile::orders).defaultIfEmpty(profile))
                .flatMap(profile -> userVouchers.map(profile::vouchers).defaultIfEmpty(profile))
                .subscribe(result -> {
                    LOGGER.info("result: " + result);
                });

        // Wait all operations to finish
        Thread.sleep(100);
    }

    public static void main(String... args) throws InterruptedException {
        LOGGER.warning("No call to order or voucher API");
        test("00");
        LOGGER.warning("One call to order and none to voucher API");
        test("10");
        LOGGER.warning("No call to order but one to voucher API");
        test("01");
        LOGGER.warning("Call both order and voucher API");
        test("11");
    }

    private static class PseudoTimer {
        final long start = System.currentTimeMillis();

        public long time() {
            return System.currentTimeMillis() - start;
        }
    }

    /*
     * Mock APIS
     */

    /**
     * Simulate user details service: expects user ids with at least 2 characters.
     * If and only if the first  character is 1, the user will be considered signed up.
     * If and only if the second character is 1, the user will be considered verified.
     */
    private static class UserServiceApi {
        Mono<UserDetailsResponse> getUserDetails(String userId) {
            return Mono.just(
                    new UserDetailsResponse(userId.charAt(0) == '1', userId.charAt(1) == '1')
            );
        }
    }

    private static class OrderServiceApi {
        Mono<OrdersResponse> getOrdersByUserId(String userId) { return Mono.just(new OrdersResponse()); }
    }

    private static class VoucherServiceApi {
        Mono<VouchersResponse> getVouchers(String userId) { return Mono.just(new VouchersResponse()); }
    }

    private static class UserDetailsResponse {

        private final boolean isSignupComplete;
        private final boolean isVerified;

        private UserDetailsResponse(boolean isSignupComplete, boolean isVerified) {
            this.isSignupComplete = isSignupComplete;
            this.isVerified = isVerified;
        }

        boolean isSignupComplete() { return isSignupComplete; }

        boolean isVerified() { return isVerified; }

        public String toString() {
            return String.format("signed: %b ; verified: %b", isSignupComplete, isVerified);
        }
    }

    private static class OrdersResponse {}

    private static class VouchersResponse {}

    private static class UserProfile {
        final String userId;
        final UserDetailsResponse details;
        final OrdersResponse orders;
        final VouchersResponse vouchers;

        public UserProfile(String userId) {
            this(userId, null, null, null);
        }

        public UserProfile(final String userId, UserDetailsResponse details, OrdersResponse orders, VouchersResponse vouchers) {
            this.userId = userId;
            this.details = details;
            this.orders = orders;
            this.vouchers = vouchers;
        }

        public UserProfile details(final UserDetailsResponse details) {
            return new UserProfile(userId, details, orders, vouchers);
        }

        public UserProfile orders(final OrdersResponse orders) {
            return new UserProfile(userId, details, orders, vouchers);
        }

        public UserProfile vouchers(final VouchersResponse vouchers) {
            return new UserProfile(userId, details, orders, vouchers);
        }

        public String toString() {
            return String.format(
                    "User %s ; %s ; Orders fetched: %b ; Vouchers fetched: %b",
                    userId, details, orders != null, vouchers != null
            );
        }
    }
}
