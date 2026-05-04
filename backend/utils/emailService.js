const nodemailer = require('nodemailer');

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASSWORD
    }
});

const sendVerificationEmail = async (email, code) => {
    await transporter.sendMail({
        from: process.env.EMAIL_USER,
        to: email,
        subject: 'Jardinito - kod weryfikacyjny',
        html: `
            <h2>Witaj w Jardinito!</h2>
            <p>Twój kod weryfikacyjny to:</p>
            <h1 style="letter-spacing: 8px">${code}</h1>
            <p>Kod jest ważny przez 2 minuty.</p>
        `
    });
};

module.exports = { sendVerificationEmail };