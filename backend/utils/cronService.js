const cron = require('node-cron');
const User = require('../models/User');
const VerificationToken = require('../models/VerificationToken');

const startCronJobs = () => {
//Raz dziennie
    cron.schedule('0 2 * * *', async () => {
//Co minutę
//cron.schedule('* * * * *', async () => {
    console.log('[CRON] Running at', new Date().toISOString());
    try {
        const expiredTokens = await VerificationToken.find({
            type: 'email_verification',
            accountExpiry: { $lt: new Date() }
        });
        console.log('[CRON] Found expired tokens:', expiredTokens.length);
            if (expiredTokens.length > 0) {
                const userIds = expiredTokens.map(t => t.userId);

                await User.deleteMany({ _id: { $in: userIds } });
                await VerificationToken.deleteMany({ _id: { $in: expiredTokens.map(t => t._id) } });

                console.log(`[CRON] Deleted ${expiredTokens.length} unverified accounts`);
            }
        } catch (err) {
            console.error('[CRON] Error:', err);
        }
    });

    console.log('[CRON] Jobs started');
};

module.exports = { startCronJobs };