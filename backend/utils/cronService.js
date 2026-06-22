const cron = require('node-cron');
const userRepository = require('../repositories/userRepository');
const verificationTokenRepository = require('../repositories/verificationTokenRepository');

const startCronJobs = () => {
    // Raz dziennie
    cron.schedule('0 2 * * *', async () => {
    // Co minutę (dev)
//     cron.schedule('* * * * *', async () => {
        console.log('[CRON] Running at', new Date().toISOString());
        try {
            const expiredTokens = await verificationTokenRepository.findExpiredEmailVerification();
            console.log('[CRON] Found expired tokens:', expiredTokens.length);

            if (expiredTokens.length > 0) {
                const userIds = expiredTokens.map(t => t.userId);
                const tokenIds = expiredTokens.map(t => t._id);

                await userRepository.deleteManyByIds(userIds);
                await verificationTokenRepository.deleteManyByIds(tokenIds);

                console.log(`[CRON] Deleted ${expiredTokens.length} unverified accounts`);
            }
        } catch (err) {
            console.error('[CRON] Error:', err);
        }
    });

    console.log('[CRON] Jobs started');
};

module.exports = { startCronJobs };