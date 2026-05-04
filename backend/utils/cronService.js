const cron = require('node-cron');
const User = require('../models/User');

const startCronJobs = () => {
    // Uruchamia się raz dziennie
//    cron.schedule('0 2 * * *', async () => {
    // Uruchamia się co minutę
    cron.schedule('* * * * *', async () => {

        try {
            const result = await User.deleteMany({
                isVerified: false,
                accountExpiry: { $lt: new Date() }
            });

            if (result.deletedCount > 0) {
                console.log(`[CRON] Deleted ${result.deletedCount} unverified accounts`);
            }
        } catch (err) {
            console.error('[CRON] Error deleting unverified accounts:', err);
        }
    });

    console.log('[CRON] Jobs started');
};

module.exports = { startCronJobs };