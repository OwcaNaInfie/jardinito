const UserTags = require('../models/UserTags');

exports.findByUserId = (userId) => UserTags.findOne({ userId });

exports.findByTagId = (tagId) => UserTags.findOne({ 'tags._id': tagId });

exports.createForUser = (userId, tags) => UserTags.create({ userId, tags });

exports.addTag = (userId, tagData) =>
    UserTags.findOneAndUpdate(
        { userId },
        { $push: { tags: tagData } },
        { new: true, upsert: true }
    );

exports.updateTag = (userId, tagId, { name, color }) =>
    UserTags.findOneAndUpdate(
        { userId, 'tags._id': tagId },
        { $set: { 'tags.$.name': name, 'tags.$.color': color } },
        { new: true }
    );

exports.removeTag = (userId, tagId) =>
    UserTags.findOneAndUpdate(
        { userId },
        { $pull: { tags: { _id: tagId } } },
        { new: true }
    );

exports.saveReordered = async (userTags, reordered) => {
    userTags.tags = reordered;
    await userTags.save();
    return userTags.tags;
};