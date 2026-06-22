const Session = require('../models/Session');

exports.create = (data) => Session.create(data);

exports.findWithFilters = (filter) =>
    Session.find(filter).populate('plantId').sort({ startedAt: -1 });

exports.findByUserIdSince = (userId, from) =>
    Session.find({ userId, startedAt: { $gte: from } }).populate('plantId');

exports.findByIdAndUpdate = (sessionId, data) =>
    Session.findByIdAndUpdate(sessionId, data);

exports.populate = (session) => session.populate('plantId');

exports.updateTagSnapshot = (userId, tagId, { name, color }) =>
    Session.updateMany(
        { userId, 'tag.tagId': tagId },
        { $set: { 'tag.name': name, 'tag.color': color } }
    );