const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

router.get('/check-username', authController.checkUsername);
router.get('/check-email', authController.checkEmail);
router.post('/register', authController.register);
router.post('/login', authController.login);
router.post('/google', authController.googleLogin);
router.post('/verify-email', authController.verifyEmail);
router.post('/resend-verification', authController.resendVerification);
router.post('/get-user-id', authController.getUserId);
router.post('/forgot-password', authController.forgotPassword);
router.post('/reset-password', authController.resetPassword);

module.exports = router;